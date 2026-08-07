package com.platformcommons.matching.api.dto;

import com.platformcommons.matching.domain.workorder.OrderPriority;
import com.platformcommons.matching.domain.workorder.WorkOrderStatus;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 工单响应 DTO。
 */
public record WorkOrderResponse(
        Long id,
        String orderNo,
        WorkOrderType orderType,
        String title,
        String description,
        Long memberId,
        Long workerId,
        String chamber,
        BigDecimal amount,
        WorkOrderStatus status,
        OrderPriority priority,
        Double locationLat,
        Double locationLng,
        Instant scheduledAt,
        Instant acceptedAt,
        Instant startedAt,
        Instant submittedAt,
        Instant completedAt,
        Instant cancelledAt,
        String cancelReason,
        Instant createdAt,
        Instant updatedAt
) {
}
