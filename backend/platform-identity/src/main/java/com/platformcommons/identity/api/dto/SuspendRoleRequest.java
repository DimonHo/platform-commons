package com.platformcommons.identity.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 暂停角色请求 DTO。
 */
public record SuspendRoleRequest(
        @NotBlank(message = "暂停原因不能为空")
        String reason
) {
}
