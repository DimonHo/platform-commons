package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.CardStatus;

import java.time.Instant;

/**
 * 银行卡响应。
 */
public record BankCardResponse(
        Long id,
        Long memberId,
        String holderName,
        String cardNoMasked,
        String bankName,
        String cardType,
        Boolean isDefault,
        CardStatus status,
        Instant boundAt
) {
}
