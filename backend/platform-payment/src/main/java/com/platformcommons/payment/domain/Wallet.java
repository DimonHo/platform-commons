package com.platformcommons.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 钱包领域模型（不可变记录）。
 *
 * @param id           钱包 ID
 * @param memberId     会员 ID
 * @param balance      可用余额
 * @param frozenAmount 冻结金额
 * @param status       钱包状态
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 */
public record Wallet(
        Long id,
        Long memberId,
        BigDecimal balance,
        BigDecimal frozenAmount,
        WalletStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
