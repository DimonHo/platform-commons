package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 创建工单请求。
 *
 * @param memberId    需求方会员 ID
 * @param orderType   工单类型（LABOR / SERVICE / DELIVERY / RIDE_HAIL / MUTUAL_ASSIST）
 * @param title       标题
 * @param description 描述
 * @param amount      金额（必须为正）
 * @param locationLat 纬度
 * @param locationLng 经度
 * @param scheduledAt 预约时间
 * @param priority    优先级（NORMAL / HIGH / URGENT）
 */
public record CreateOrderRequest(
        @NotNull Long memberId,
        @NotBlank String orderType,
        @NotBlank String title,
        String description,
        @NotNull @Positive BigDecimal amount,
        Double locationLat,
        Double locationLng,
        Instant scheduledAt,
        String priority
) {
}
