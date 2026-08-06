package com.platformcommons.matching.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 工单响应 DTO。
 */
public record WorkOrderResponse(
        Long id,
        String orderNo,
        String orderType,
        String title,
        String description,
        Long memberId,
        Long workerId,
        String chamber,
        BigDecimal amount,
        String status,
        String priority,
        Double locationLat,
        Double locationLng,
        Instant scheduledAt,
        Instant acceptedAt,
        Instant startedAt,
        Instant submittedAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancelReason,
        Instant createdAt,
        Instant updatedAt
) {
}
