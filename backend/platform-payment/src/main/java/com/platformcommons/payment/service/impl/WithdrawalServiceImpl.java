package com.platformcommons.payment.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.payment.domain.ChannelCode;
import com.platformcommons.payment.domain.Wallet;
import com.platformcommons.payment.domain.WalletBusinessType;
import com.platformcommons.payment.domain.WithdrawalRecord;
import com.platformcommons.payment.domain.WithdrawalRequest;
import com.platformcommons.payment.domain.WithdrawalStatus;
import com.platformcommons.payment.repository.WithdrawalRecordRepository;
import com.platformcommons.payment.repository.WithdrawalRequestRepository;
import com.platformcommons.payment.repository.entity.WithdrawalRecordEntity;
import com.platformcommons.payment.repository.entity.WithdrawalRequestEntity;
import com.platformcommons.payment.service.WalletService;
import com.platformcommons.payment.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 提现服务实现。
 *
 * <p>提现流程：申请冻结 → 审核 → 完成扣款 / 拒绝解冻。
 * 金额比较必须使用 {@link BigDecimal#compareTo(Object)}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final WalletService walletService;

    @Override
    public WithdrawalRequest requestWithdrawal(Long memberId, Long bankCardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "提现金额必须为正数");
        }
        Wallet wallet = walletService.getOrCreateWallet(memberId);

        // 冻结金额（从余额转移到冻结金额）
        walletService.freeze(memberId, amount, "WITHDRAWAL", null);

        Instant now = Instant.now();
        WithdrawalRequestEntity entity = new WithdrawalRequestEntity();
        entity.setRequestNo(SnowflakeUtils.nextId("WD"));
        entity.setMemberId(memberId);
        entity.setWalletId(wallet.id());
        entity.setBankCardId(bankCardId);
        entity.setAmount(amount);
        entity.setFee(BigDecimal.ZERO);
        entity.setStatus(WithdrawalStatus.PENDING);
        entity.setRiskScore(0);
        entity.setAppliedAt(now);
        withdrawalRequestRepository.save(entity);
        log.info("Withdrawal requested: requestNo={}, memberId={}, amount={}", entity.getRequestNo(), memberId, amount);
        return toDomain(entity);
    }

    @Override
    public WithdrawalRequest approveWithdrawal(Long withdrawalId, Long reviewerId) {
        WithdrawalRequestEntity entity = requireEntity(withdrawalId);
        if (entity.getStatus() != WithdrawalStatus.PENDING) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "提现申请状态不允许审核: " + entity.getStatus());
        }
        entity.setStatus(WithdrawalStatus.APPROVED);
        entity.setReviewedAt(Instant.now());
        entity.setReviewerId(reviewerId);
        withdrawalRequestRepository.save(entity);
        log.info("Withdrawal approved: id={}, reviewerId={}", withdrawalId, reviewerId);
        return toDomain(entity);
    }

    @Override
    public WithdrawalRequest rejectWithdrawal(Long withdrawalId, Long reviewerId, String reason) {
        WithdrawalRequestEntity entity = requireEntity(withdrawalId);
        if (entity.getStatus() != WithdrawalStatus.PENDING && entity.getStatus() != WithdrawalStatus.APPROVED) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "提现申请状态不允许拒绝: " + entity.getStatus());
        }
        entity.setStatus(WithdrawalStatus.REJECTED);
        entity.setReviewedAt(Instant.now());
        entity.setReviewerId(reviewerId);
        entity.setRejectReason(reason);
        entity.setCompletedAt(Instant.now());

        // 拒绝后解冻金额（解冻钱包状态，金额回滚需要再充值；简化处理：通过 refund 回滚）
        // 注意：freeze 已经从 balance 转移到 frozenAmount，拒绝时应将金额返还
        refundFrozen(entity.getMemberId(), entity.getAmount(), entity.getRequestNo());

        withdrawalRequestRepository.save(entity);
        log.info("Withdrawal rejected: id={}, reviewerId={}, reason={}", withdrawalId, reviewerId, reason);
        return toDomain(entity);
    }

    @Override
    public WithdrawalRequest completeWithdrawal(Long withdrawalId, String channelTransferNo) {
        WithdrawalRequestEntity entity = requireEntity(withdrawalId);
        if (entity.getStatus() != WithdrawalStatus.APPROVED) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "提现申请状态不允许完成: " + entity.getStatus());
        }

        Instant now = Instant.now();
        // 实际扣减钱包（frozenAmount 已经在申请时冻结，此处真正扣减）
        walletService.deduct(entity.getMemberId(), entity.getAmount(), WalletBusinessType.WITHDRAW,
                "WITHDRAWAL", entity.getRequestNo(), "提现扣款: " + entity.getRequestNo());

        entity.setStatus(WithdrawalStatus.SUCCESS);
        entity.setCompletedAt(now);
        withdrawalRequestRepository.save(entity);

        // 创建提现记录
        WithdrawalRecordEntity record = new WithdrawalRecordEntity();
        record.setWithdrawalRequestId(entity.getId());
        record.setChannelCode(ChannelCode.BANK_TRANSFER);
        record.setChannelMerchant("DEMO_BANK");
        record.setChannelTransferNo(channelTransferNo);
        record.setChannelRespCode("SUCCESS");
        record.setChannelRespMsg("转账成功");
        record.setStatus("SUCCESS");
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        withdrawalRecordRepository.save(record);

        log.info("Withdrawal completed: id={}, channelTransferNo={}", withdrawalId, channelTransferNo);
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalRequest> listMemberWithdrawals(Long memberId) {
        return withdrawalRequestRepository.findByMemberIdOrderByAppliedAtDesc(memberId).stream()
                .map(this::toDomain)
                .toList();
    }

    // ===== helpers =====

    private WithdrawalRequestEntity requireEntity(Long id) {
        return withdrawalRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "提现申请不存在: id=" + id));
    }

    /**
     * 将冻结的金额回滚到余额（用于拒绝提现场景）。
     *
     * <p>由于 {@code freeze} 在申请时将 amount 从 balance 转移到 frozenAmount，
     * 拒绝时需要做逆操作。这里通过内部方法直接操作钱包流水记录一笔 REFUND。</p>
     */
    private void refundFrozen(Long memberId, BigDecimal amount, String refId) {
        // 使用 REFUND 业务类型回滚：充值方式把冻结金额还回余额
        walletService.recharge(memberId, amount, "提现拒绝-金额退回: " + refId);
    }

    private WithdrawalRequest toDomain(WithdrawalRequestEntity e) {
        return new WithdrawalRequest(e.getId(), e.getRequestNo(), e.getMemberId(), e.getWalletId(),
                e.getBankCardId(), e.getAmount(), e.getFee(), e.getStatus(), e.getRiskScore(),
                e.getRejectReason(), e.getAppliedAt(), e.getReviewedAt(), e.getReviewerId(), e.getCompletedAt());
    }
}
