package com.platformcommons.payment.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.payment.application.PaymentService;
import com.platformcommons.payment.application.WalletService;
import com.platformcommons.payment.domain.transaction.LedgerEvent;
import com.platformcommons.payment.domain.transaction.LedgerEventEntity;
import com.platformcommons.payment.domain.transaction.LedgerEventRepository;
import com.platformcommons.payment.domain.transaction.SettlementResult;
import com.platformcommons.payment.domain.transaction.SettlementRule;
import com.platformcommons.payment.domain.transaction.Transaction;
import com.platformcommons.payment.domain.transaction.TransactionEntity;
import com.platformcommons.payment.domain.transaction.TransactionRepository;
import com.platformcommons.payment.domain.transaction.TransactionStatus;
import com.platformcommons.payment.domain.wallet.WalletBusinessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 支付与分账服务实现。
 *
 * <p>阿里规范：{@code @Override} 不省略；包装类比较使用 {@code equals()}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    /** 百分比精度：4 位小数。 */
    private static final int SCALE = 4;

    private final TransactionRepository transactionRepository;
    private final LedgerEventRepository ledgerEventRepository;
    private final WalletService walletService;

    @Override
    public Transaction charge(String orderId, String workerId, String requesterId, BigDecimal grossAmount) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        if (grossAmount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "订单金额必须为正数: " + grossAmount);
        }

        // 幂等检查：order_id 唯一约束兜底
        transactionRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "订单已创建交易: " + orderId);
        });

        Instant now = Instant.now();
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrderId(orderId);
        entity.setWorkerId(workerId);
        entity.setRequesterId(requesterId);
        entity.setGrossAmount(grossAmount);
        entity.setPlatformFee(BigDecimal.ZERO);
        entity.setWorkerShare(BigDecimal.ZERO);
        entity.setStatus(TransactionStatus.PENDING);
        entity.setRuleVersion(SettlementRule.DEFAULT.version());
        entity.setCreatedAt(now);
        transactionRepository.save(entity);

        // 记录收款事件
        persistEvent(new LedgerEvent.ChargeCreated(UUID.randomUUID(), entity.getId(), grossAmount, now));
        log.info("Transaction charged: txId={}, orderId={}, grossAmount={}", entity.getId(), orderId, grossAmount);

        return toDomain(entity);
    }

    @Override
    public SettlementResult settle(UUID transactionId) {
        TransactionEntity entity = requireTransaction(transactionId);
        if (!TransactionStatus.CHARGED.equals(entity.getStatus()) && !TransactionStatus.PENDING.equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "交易状态不允许结算: " + entity.getStatus());
        }

        SettlementRule rule = SettlementRule.DEFAULT;
        BigDecimal platformFee = entity.getGrossAmount().multiply(rule.platformFeeRate()).setScale(SCALE, RoundingMode.HALF_UP);
        // 可分配结余（gross - platformFee）全额返还劳动者，保证金额守恒：gross = platformFee + workerShare
        BigDecimal workerShare = entity.getGrossAmount().subtract(platformFee);
        BigDecimal workerRatio = workerShare.divide(entity.getGrossAmount(), SCALE, RoundingMode.HALF_UP);

        boolean compliant = workerRatio.compareTo(rule.minWorkerShareRate()) >= 0;
        if (!compliant) {
            log.warn("Settlement below anti-exploitation floor: txId={}, ratio={}", transactionId, workerRatio);
        }

        Instant now = Instant.now();
        entity.setPlatformFee(platformFee);
        entity.setWorkerShare(workerShare);
        entity.setStatus(TransactionStatus.SETTLED);
        entity.setSettledAt(now);
        transactionRepository.save(entity);

        // 钱包入账：扣需求方订单总额，劳动者入账劳动所得（同一事务，任一失败整体回滚）
        Long requesterId = Long.parseLong(entity.getRequesterId());
        Long workerId = Long.parseLong(entity.getWorkerId());
        walletService.deduct(requesterId, entity.getGrossAmount(), WalletBusinessType.SETTLE,
                "PaymentTransaction", entity.getOrderId(), "订单结算扣款");
        walletService.income(workerId, workerShare, WalletBusinessType.SETTLE,
                "PaymentTransaction", entity.getOrderId(), "劳动所得入账");

        persistEvent(new LedgerEvent.SettlementCompleted(UUID.randomUUID(), transactionId, workerShare, now));
        log.info("Transaction settled: txId={}, workerShare={}, ratio={}, compliant={}",
                transactionId, workerShare, workerRatio, compliant);

        return new SettlementResult(
                transactionId, entity.getGrossAmount(), platformFee, workerShare,
                workerShare, workerRatio, rule.version(), compliant
        );
    }

    @Override
    public Transaction refund(UUID transactionId) {
        TransactionEntity entity = requireTransaction(transactionId);
        if (TransactionStatus.REFUNDED.equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "交易已退款: " + transactionId);
        }

        Instant now = Instant.now();
        entity.setStatus(TransactionStatus.REFUNDED);
        entity.setSettledAt(now);
        transactionRepository.save(entity);

        persistEvent(new LedgerEvent.RefundIssued(UUID.randomUUID(), transactionId, entity.getGrossAmount(), now));
        log.info("Transaction refunded: txId={}, amount={}", transactionId, entity.getGrossAmount());

        return toDomain(entity);
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return transactionRepository.findById(transactionId).map(this::toDomain);
    }

    private TransactionEntity requireTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "交易不存在: " + transactionId));
    }

    private Transaction toDomain(TransactionEntity e) {
        return new Transaction(
                e.getId(), e.getOrderId(), e.getWorkerId(), e.getRequesterId(),
                e.getGrossAmount(), e.getPlatformFee(), e.getWorkerShare(),
                e.getStatus(), e.getRuleVersion(), e.getCreatedAt(), e.getSettledAt()
        );
    }

    private void persistEvent(LedgerEvent event) {
        BigDecimal amount = switch (event) {
            case LedgerEvent.ChargeCreated c -> c.grossAmount();
            case LedgerEvent.SettlementCompleted s -> s.workerShare();
            case LedgerEvent.RefundIssued r -> r.refundAmount();
        };
        ledgerEventRepository.save(new LedgerEventEntity(
                event.eventId(), event.transactionId(), event.eventType(), amount, event.occurredAt()
        ));
    }
}
