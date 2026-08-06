package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 提交实名认证请求 DTO。
 */
public record SubmitVerificationRequest(
        @NotBlank(message = "真实姓名不能为空")
        String realName,

        @NotBlank(message = "证件类型不能为空")
        String idCardType,

        @NotBlank(message = "证件号码不能为空")
        String idCardNo,

        String verificationChannel
) {
}
