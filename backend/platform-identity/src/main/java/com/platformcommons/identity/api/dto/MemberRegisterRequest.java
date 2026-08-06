package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 成员注册请求 DTO。
 *
 * <p>角色不在此处提交，通过 ProfileController 的 /api/members/{memberId}/roles 端点申请。</p>
 *
 * @param name   姓名
 * @param phone  手机号（中国大陆 11 位）
 */
public record MemberRegisterRequest(
        @NotBlank(message = "姓名不能为空")
        @Size(max = 64, message = "姓名长度不能超过 64")
        String name,

        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone
) {
}
