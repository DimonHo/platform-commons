package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 申请角色请求 DTO。
 */
public record ApplyRoleRequest(
        @NotBlank(message = "角色类型不能为空")
        String roleType
) {
}
