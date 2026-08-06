package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 钱包响应。
 */
public record WalletResponse(
        Long id,
        Long memberId,
        BigDecimal balance,
        BigDecimal frozenAmount,
        WalletStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
