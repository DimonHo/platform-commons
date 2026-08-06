package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.PaymentOrderDirection;
import com.platformcommons.payment.domain.PaymentOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付订单响应。
 */
public record PaymentOrderResponse(
        Long id,
        String orderNo,
        Long memberId,
        PaymentOrderDirection direction,
        BigDecimal amount,
        String businessType,
        String refType,
        String refId,
        PaymentOrderStatus status,
        Instant expireAt,
        Instant createdAt,
        Instant paidAt
) {
}
