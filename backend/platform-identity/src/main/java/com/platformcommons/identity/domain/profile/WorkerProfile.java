package com.platformcommons.identity.domain.profile;

/**
 * 劳动者档案领域模型（不可变 record）。
 *
 * @param id                主键
 * @param memberId          成员 ID
 * @param serviceCategories 服务类目（逗号分隔）
 * @param serviceRadiusM    服务半径（米）
 * @param vehicleType       车辆类型
 * @param vehiclePlate      车牌号
 * @param skills            技能描述
 * @param maxConcurrent     最大并发接单数
 * @param rating            评分
 * @param totalCompleted    累计完成数
 * @param onlineStatus      在线状态
 * @param bio               个人简介
 */
public record WorkerProfile(
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
