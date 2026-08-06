package com.platformcommons.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 提现申请领域模型（不可变记录）。
 *
 * @param id                  提现申请 ID
 * @param requestNo           申请单号
 * @param memberId            会员 ID
 * @param walletId            钱包 ID
 * @param bankCardId          银行卡 ID
 * @param amount              提现金额
 * @param fee                 手续费
 * @param status              提现状态
 * @param riskScore           风控分
 * @param rejectReason        拒绝原因
 * @param appliedAt           申请时间
 * @param reviewedAt          审核时间
 * @param reviewerId          审核人 ID
 * @param completedAt         完成时间
 */
public record WithdrawalRequest(
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
