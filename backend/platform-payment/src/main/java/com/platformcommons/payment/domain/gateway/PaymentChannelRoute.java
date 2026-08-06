package com.platformcommons.payment.domain.gateway;

import java.time.Instant;

/**
 * 支付渠道路由领域模型（不可变记录）。
 *
 * @param id              路由 ID
 * @param paymentOrderId  支付订单 ID
 * @param channelCode     渠道编码
 * @param channelMerchant 渠道商户号
 * @param channelOrderNo  渠道订单号
 * @param channelRespCode 渠道响应码
 * @param channelRespMsg  渠道响应消息
 * @param status          路由状态
 * @param attemptCount    尝试次数
 * @param createdAt       创建时间
 * @param updatedAt       更新时间
 */
public record PaymentChannelRoute(
        Long id,
        Long paymentOrderId,
        ChannelCode channelCode,
        String channelMerchant,
        String channelOrderNo,
        String channelRespCode,
        String channelRespMsg,
        ChannelRouteStatus status,
        Integer attemptCount,
        Instant createdAt,
        Instant updatedAt
) {
}
