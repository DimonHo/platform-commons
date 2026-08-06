package com.platformcommons.identity.api.dto;

import com.platformcommons.identity.domain.profile.VehicleType;
import com.platformcommons.identity.domain.profile.WorkerOnlineStatus;

/**
 * 劳动者档案响应 DTO。
 */
public record WorkerProfileResponse(
        Long id,
        Long memberId,
        String serviceCategories,
        Integer serviceRadiusM,
        VehicleType vehicleType,
        String vehiclePlate,
        String skills,
        Integer maxConcurrent,
        Double rating,
        Integer totalCompleted,
        WorkerOnlineStatus onlineStatus,
        String bio
) {
}
