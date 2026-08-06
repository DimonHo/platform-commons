package com.platformcommons.matching.domain.dispatch;

import com.platformcommons.matching.domain.workorder.WorkOrderType;

import java.time.Instant;

/**
 * 派单广播领域模型（不可变）。
 *
 * @param id            主键
 * @param broadcastNo   广播号（雪花 ID，BCAST 前缀）
 * @param orderId       关联工单 ID
 * @param orderType     工单类型
 * @param broadcastType 广播类型
 * @param centerLat     中心纬度
 * @param centerLng     中心经度
 * @param radiusMeters  半径（米）
 * @param targetCount   目标抢单人数
 * @param grabbedCount  已抢到人数
 * @param status        状态
 * @param expireAt      过期时间
 * @param createdAt     创建时间
 */
public record DispatchBroadcast(
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
