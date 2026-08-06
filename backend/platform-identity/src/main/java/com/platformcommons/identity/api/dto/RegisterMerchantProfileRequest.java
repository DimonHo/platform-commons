package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 注册商家档案请求 DTO。
 */
public record RegisterMerchantProfileRequest(
        @NotNull(message = "成员 ID 不能为空")
        Long memberId,

        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 128, message = "店铺名称长度不能超过 128")
        String shopName,

        @NotBlank(message = "店铺分类不能为空")
        @Size(max = 64, message = "店铺分类长度不能超过 64")
        String shopCategory,

        @Size(max = 64, message = "营业执照号长度不能超过 64")
        String businessLicense,

        @Size(max = 512, message = "执照照片 URL 长度不能超过 512")
        String licensePhotoUrl,

        @NotBlank(message = "店铺地址不能为空")
        @Size(max = 256, message = "店铺地址长度不能超过 256")
        String shopAddress,

        Double shopLat,

        Double shopLng,

        @Size(max = 128, message = "营业时间长度不能超过 128")
        String businessHours,

        Integer deliveryRadiusM
) {
}
