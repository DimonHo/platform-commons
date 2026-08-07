package com.platformcommons.matching.api.dto;

import com.platformcommons.matching.domain.dispatch.BroadcastStatus;
import com.platformcommons.matching.domain.dispatch.BroadcastType;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import java.time.Instant;

/**
 * 派单广播响应 DTO。
 */
public record DispatchBroadcastResponse(
        Long id,
        String broadcastNo,
        Long orderId,
        WorkOrderType orderType,
        BroadcastType broadcastType,
        Double centerLat,
        Double centerLng,
        Integer radiusMeters,
        Integer targetCount,
        Integer grabbedCount,
        BroadcastStatus status,
        Instant expireAt,
        Instant createdAt
) {
}
