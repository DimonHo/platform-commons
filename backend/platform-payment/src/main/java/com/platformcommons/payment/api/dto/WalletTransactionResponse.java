package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.TransactionDirection;
import com.platformcommons.payment.domain.WalletBusinessType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 钱包流水响应。
 */
public record WalletTransactionResponse(
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
