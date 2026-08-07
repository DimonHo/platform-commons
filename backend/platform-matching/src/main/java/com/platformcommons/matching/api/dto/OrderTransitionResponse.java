package com.platformcommons.matching.api.dto;

import com.platformcommons.matching.domain.workorder.OperatorRole;
import com.platformcommons.matching.domain.workorder.TransitionAction;
import com.platformcommons.matching.domain.workorder.WorkOrderStatus;
import java.time.Instant;

/**
 * 工单流转记录响应 DTO。
 */
public record OrderTransitionResponse(
        Long id,
        Long orderId,
        WorkOrderStatus fromStatus,
        WorkOrderStatus toStatus,
        TransitionAction action,
        Long operatorId,
        OperatorRole operatorRole,
        String remark,
        String attachmentUrls,
        Instant createdAt
) {
}
