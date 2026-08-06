package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.withdrawal.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 提现申请响应。
 */
public record WithdrawalResponse(
        Long id,
        String requestNo,
        Long memberId,
        Long walletId,
        Long bankCardId,
        BigDecimal amount,
        BigDecimal fee,
        WithdrawalStatus status,
        Integer riskScore,
        String rejectReason,
        Instant appliedAt,
        Instant reviewedAt,
        Long reviewerId,
        Instant completedAt
) {
}
