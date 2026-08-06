package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建派单广播请求。
 *
 * @param orderId       关联工单 ID
 * @param orderType     工单类型
 * @param centerLat     中心纬度
 * @param centerLng     中心经度
 * @param radiusMeters  半径（米）
 * @param targetCount   目标抢单人数（默认 1）
 * @param broadcastType 广播类型（GRAB / ASSIGN）
 */
public record CreateBroadcastRequest(
        @NotNull Long orderId,
        @NotBlank String orderType,
        @NotNull Double centerLat,
        @NotNull Double centerLng,
        Integer radiusMeters,
        Integer targetCount,
        @NotBlank String broadcastType
) {
}
