package com.platformcommons.matching.api.dto;

import java.time.Instant;

/**
 * 工单流转记录响应 DTO。
 */
public record OrderTransitionResponse(
        Long id,
        Long orderId,
        String fromStatus,
        String toStatus,
        String action,
        Long operatorId,
        String operatorRole,
        String remark,
        String attachmentUrls,
        Instant createdAt
) {
}
