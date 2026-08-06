package com.platformcommons.matching.api.dto;

import java.time.Instant;

/**
 * 派单广播响应 DTO。
 */
public record DispatchBroadcastResponse(
        Long id,
        String broadcastNo,
        Long orderId,
        String orderType,
        String broadcastType,
        Double centerLat,
        Double centerLng,
        Integer radiusMeters,
        Integer targetCount,
        Integer grabbedCount,
        String status,
        Instant expireAt,
        Instant createdAt
) {
}
