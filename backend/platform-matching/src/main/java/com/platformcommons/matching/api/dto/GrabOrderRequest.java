package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 抢单请求。
 *
 * @param workerId  劳动者 ID
 * @param workerLat 劳动者纬度
 * @param workerLng 劳动者经度
 */
public record GrabOrderRequest(
        @NotNull Long workerId,
        @NotNull Double workerLat,
        @NotNull Double workerLng
) {
}
