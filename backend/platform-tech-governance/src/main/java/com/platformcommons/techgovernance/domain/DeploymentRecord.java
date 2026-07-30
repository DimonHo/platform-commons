package com.platformcommons.techgovernance.domain;

/**
 * 部署记录（第13章 第70-73条）
 * <p>
 * 可复现构建核验：每次部署须关联提交哈希与构建制品哈希，
 * 确保生产环境运行的代码与代码库版本一一对应。
 *
 * @param deploymentId      部署编号
 * @param commitHash        提交哈希（SHA-256）
 * @param buildArtifactHash 构建制品哈希（SHA-256）
 * @param configDigest      部署配置摘要（哈希）
 * @param deployedAt        部署时间戳（ISO-8601）
 * @param deployedBy        部署操作人
 */
public record DeploymentRecord(
        String deploymentId,
        String commitHash,
        String buildArtifactHash,
        String configDigest,
        String deployedAt,
        String deployedBy
) {
}
