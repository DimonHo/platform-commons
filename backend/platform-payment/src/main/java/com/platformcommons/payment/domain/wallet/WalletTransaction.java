package com.platformcommons.payment.domain.wallet;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 钱包流水领域模型（不可变记录）。
 *
 * @param id            流水 ID
 * @param walletId      钱包 ID
 * @param memberId      会员 ID
 * @param transactionNo 流水号
 * @param direction     收支方向
 * @param amount        金额
 * @param balanceAfter  操作后余额
 * @param businessType  业务类型
 * @param refType       关联业务类型
 * @param refId         关联业务 ID
 * @param remark        备注
 * @param createdAt     创建时间
 */
public record WalletTransaction(
        Long id,
        Long walletId,
        Long memberId,
        String transactionNo,
        TransactionDirection direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        WalletBusinessType businessType,
        String refType,
        String refId,
        String remark,
        Instant createdAt
) {
}
