package com.platformcommons.matching.api.dto;

import java.time.Instant;

/**
 * 抢单记录响应 DTO。
 */
public record DispatchGrabRecordResponse(
        Long id,
        Long broadcastId,
        Long workerId,
        Double workerLat,
        Double workerLng,
        Integer distanceMeters,
        String status,
        Instant grabbedAt
) {
}
