package com.platformcommons.dispute.domain;

/**
 * 争议记录（第15章 第93-96条）
 * <p>
 * 三级救济流程的争议实体，记录申诉人、事由、当前层级和处理结果。
 *
 * @param disputeId    争议编号
 * @param filedBy      申诉人编号
 * @param subject      争议事由
 * @param description  详细描述
 * @param level        当前救济层级
 * @param status       当前状态
 * @param resolution   裁决/处理结果（如有）
 * @param filedAt      提交时间
 */
public record Dispute(
        String disputeId,
        String filedBy,
        String subject,
        String description,
        DisputeLevel level,
        DisputeStatus status,
        String resolution,
        String filedAt
) {
}
