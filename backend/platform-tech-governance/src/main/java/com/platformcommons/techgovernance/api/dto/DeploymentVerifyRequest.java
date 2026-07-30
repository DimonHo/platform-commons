package com.platformcommons.techgovernance.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 部署核验请求 DTO
 *
 * @param commitHash        提交哈希
 * @param buildArtifactHash 构建制品哈希
 * @param configDigest      部署配置摘要
 * @param deployedBy        部署操作人
 */
public record DeploymentVerifyRequest(
        @NotBlank(message = "提交哈希不能为空")
        String commitHash,

        @NotBlank(message = "构建制品哈希不能为空")
        String buildArtifactHash,

        String configDigest,
        String deployedBy
) implements Serializable {
}
