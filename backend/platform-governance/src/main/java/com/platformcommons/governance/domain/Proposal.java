package com.platformcommons.governance.domain;

import java.time.LocalDateTime;

/**
 * 提案领域模型（不可变 record）。
 *
 * <p>聚合根，表达一项治理提案及其生命周期。</p>
 *
 * @param id              提案 ID
 * @param title           标题
 * @param description     详细说明
 * @param type            提案类型
 * @param status          当前状态
 * @param proposerId      提案人成员 ID
 * @param targetChamber   目标表决议院（null 表示全员表决）
 * @param votingStartAt   投票开始时间
 * @param votingEndAt     投票截止时间
 * @param createdAt       创建时间
 */
public record Proposal(
        Long id,
        String title,
        String description,
        ProposalType type,
        ProposalStatus status,
        Long proposerId,
        GovernanceChamber targetChamber,
        LocalDateTime votingStartAt,
        LocalDateTime votingEndAt,
        LocalDateTime createdAt
) {
}
