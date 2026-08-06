package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注册劳动者档案请求 DTO。
 */
public record RegisterWorkerProfileRequest(
        @NotNull(message = "成员 ID 不能为空")
        Long memberId,

        @Size(max = 256, message = "服务类目长度不能超过 256")
        String serviceCategories,

        Integer serviceRadiusM,

        @NotBlank(message = "车辆类型不能为空")
        String vehicleType,

        @Size(max = 32, message = "车牌号长度不能超过 32")
        String vehiclePlate,

        @Size(max = 256, message = "技能描述长度不能超过 256")
        String skills,

        Integer maxConcurrent,

        @Size(max = 512, message = "个人简介长度不能超过 512")
        String bio
) {
}
