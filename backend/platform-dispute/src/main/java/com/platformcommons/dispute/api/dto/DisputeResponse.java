package com.platformcommons.dispute.api.dto;

import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.domain.DisputeStatus;

import java.io.Serializable;

/**
 * 争议响应 DTO
 *
 * @param disputeId   争议编号
 * @param filedBy     申诉人
 * @param subject     争议事由
 * @param description 详细描述
 * @param level       当前救济层级
 * @param status      当前状态
 * @param resolution  裁决结果
 * @param filedAt     提交时间
 */
public record DisputeResponse(
        String disputeId,
        String filedBy,
        String subject,
        String description,
        DisputeLevel level,
        DisputeStatus status,
        String resolution,
        String filedAt
) implements Serializable {
}
