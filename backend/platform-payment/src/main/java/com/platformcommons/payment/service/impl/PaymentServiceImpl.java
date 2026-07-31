package com.platformcommons.payment.service.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.payment.domain.LedgerEvent;
import com.platformcommons.payment.domain.SettlementResult;
import com.platformcommons.payment.domain.SettlementRule;
import com.platformcommons.payment.domain.Transaction;
import com.platformcommons.payment.domain.TransactionStatus;
import com.platformcommons.payment.repository.LedgerEventRepository;
import com.platformcommons.payment.repository.entity.LedgerEventEntity;
import com.platformcommons.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 支付与分账服务实现。
 *
 * <p>阿里规范：{@code @Override} 不省略；包装类比较使用 {@code equals()}；
 * 线程安全集合使用 {@link ConcurrentHashMap}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {


    /** 百分比精度：4 位小数。 */
    private static final int SCALE = 4;

    private final LedgerEventRepository ledgerEventRepository;

    /** 交易内存存储（演示用，生产环境应替换为 JPA 持久化）。 */
    private final Map<UUID, Transaction> transactionStore = new ConcurrentHashMap<>();


    @Override
    public Transaction charge(String orderId, String workerId, String requesterId, BigDecimal grossAmount) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        if (grossAmount.signum() <= 0) {
            throw new BusinessException("grossAmount must be positive: " + grossAmount);
        }

        UUID txId = UUID.randomUUID();
        Instant now = Instant.now();
        Transaction tx = new Transaction(
                txId, orderId, workerId, requesterId,
                grossAmount,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                TransactionStatus.PENDING,
                SettlementRule.DEFAULT.version(),
                now,
                null
        );
        transactionStore.put(txId, tx);

        // 记录收款事件
        persistEvent(new LedgerEvent.ChargeCreated(UUID.randomUUID(), txId, grossAmount, now));
        log.info("Transaction charged: txId={}, orderId={}, grossAmount={}", txId, orderId, grossAmount);

        return tx;
    }

    @Override
    public SettlementResult settle(UUID transactionId) {
        Transaction tx = requireTransaction(transactionId);
        if (!TransactionStatus.CHARGED.equals(tx.status()) && !TransactionStatus.PENDING.equals(tx.status())) {
            throw new BusinessException("transaction is not in a settleable state: " + tx.status());
        }

        SettlementRule rule = SettlementRule.DEFAULT;
        BigDecimal platformFee = tx.grossAmount().multiply(rule.platformFeeRate()).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal surplus = tx.grossAmount().subtract(platformFee);

        // 劳动者返还不低于反榨取底线（surplus * minWorkerShareRate）
        BigDecimal workerShare = surplus.multiply(rule.minWorkerShareRate()).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal workerRatio = workerShare.divide(tx.grossAmount(), SCALE, RoundingMode.HALF_UP);

        boolean compliant = workerRatio.compareTo(rule.minWorkerShareRate()) >= 0;
        if (!compliant) {
            log.warn("Settlement below anti-exploitation floor: txId={}, ratio={}", transactionId, workerRatio);
        }

        Instant now = Instant.now();
        Transaction settled = new Transaction(
                tx.id(), tx.orderId(), tx.workerId(), tx.requesterId(),
                tx.grossAmount(), platformFee, workerShare,
                TransactionStatus.SETTLED, rule.version(), tx.createdAt(), now
        );
        transactionStore.put(transactionId, settled);

        persistEvent(new LedgerEvent.SettlementCompleted(UUID.randomUUID(), transactionId, workerShare, now));
        log.info("Transaction settled: txId={}, workerShare={}, ratio={}, compliant={}",
                transactionId, workerShare, workerRatio, compliant);

        return new SettlementResult(
                transactionId, tx.grossAmount(), platformFee, surplus,
                workerShare, workerRatio, rule.version(), compliant
        );
    }

    @Override
    public Transaction refund(UUID transactionId) {
        Transaction tx = requireTransaction(transactionId);
        if (TransactionStatus.REFUNDED.equals(tx.status())) {
            throw new BusinessException("transaction already refunded: " + transactionId);
        }

        Instant now = Instant.now();
        Transaction refunded = new Transaction(
                tx.id(), tx.orderId(), tx.workerId(), tx.requesterId(),
                tx.grossAmount(), tx.platformFee(), tx.workerShare(),
                TransactionStatus.REFUNDED, tx.settlementRuleVersion(), tx.createdAt(), now
        );
        transactionStore.put(transactionId, refunded);

        persistEvent(new LedgerEvent.RefundIssued(UUID.randomUUID(), transactionId, tx.grossAmount(), now));
        log.info("Transaction refunded: txId={}, amount={}", transactionId, tx.grossAmount());

        return refunded;
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return Optional.ofNullable(transactionStore.get(transactionId));
    }

    private Transaction requireTransaction(UUID transactionId) {
        Transaction tx = transactionStore.get(transactionId);
        if (tx == null) {
            throw new BusinessException("transaction not found: " + transactionId);
        }
        return tx;
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
