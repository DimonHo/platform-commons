package com.platformcommons.payment.domain.paymentorder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付订单领域模型（不可变记录）。
 *
 * @param id           订单 ID
 * @param orderNo      订单号
 * @param memberId     会员 ID
 * @param direction    订单方向：PAY / RECHARGE
 * @param amount       金额
 * @param businessType 业务类型
 * @param refType      关联业务类型
 * @param refId        关联业务 ID
 * @param status       订单状态
 * @param expireAt     过期时间
 * @param createdAt    创建时间
 * @param paidAt       支付时间
 */
public record PaymentOrder(
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
