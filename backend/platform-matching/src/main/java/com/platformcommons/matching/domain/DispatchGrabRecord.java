package com.platformcommons.matching.domain;

import java.time.Instant;

/**
 * 抢单记录领域模型（不可变）。
 *
 * @param id              主键
 * @param broadcastId     广播 ID
 * @param workerId        劳动者 ID
 * @param workerLat       劳动者纬度
 * @param workerLng       劳动者经度
 * @param distanceMeters  与广播中心的距离（米）
 * @param status          抢单结果状态
 * @param grabbedAt       抢单时间
 */
public record DispatchGrabRecord(
        Long id,
        Long broadcastId,
        Long workerId,
        Double workerLat,
        Double workerLng,
        Integer distanceMeters,
        GrabStatus status,
        Instant grabbedAt
) {
}
