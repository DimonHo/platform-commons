package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建收货地址请求 DTO。
 */
public record CreateAddressRequest(
        @Size(max = 32, message = "地址标签长度不能超过 32")
        String label,

        @NotBlank(message = "收件人姓名不能为空")
        @Size(max = 64, message = "收件人姓名长度不能超过 64")
        String receiverName,

        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20, message = "联系电话长度不能超过 20")
        String phone,

        @NotBlank(message = "省份不能为空")
        @Size(max = 32, message = "省份长度不能超过 32")
        String province,

        @NotBlank(message = "城市不能为空")
        @Size(max = 32, message = "城市长度不能超过 32")
        String city,

        @NotBlank(message = "区县不能为空")
        @Size(max = 32, message = "区县长度不能超过 32")
        String district,

        @NotBlank(message = "详细地址不能为空")
        @Size(max = 256, message = "详细地址长度不能超过 256")
        String detail,

        Double latitude,

        Double longitude,

        Boolean isDefault
) {
}
