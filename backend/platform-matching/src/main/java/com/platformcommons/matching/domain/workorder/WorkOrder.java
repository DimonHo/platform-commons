package com.platformcommons.matching.domain.workorder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 业务工单领域模型（不可变）。
 *
 * @param id            主键
 * @param orderNo       订单号（雪花 ID，ORD 前缀）
 * @param orderType     工单类型
 * @param title         标题
 * @param description   描述
 * @param memberId      需求方会员 ID
 * @param workerId      劳动者 ID
 * @param chamber       商会/区域
 * @param amount        金额
 * @param status        状态
 * @param priority      优先级
 * @param locationLat   纬度
 * @param locationLng   经度
 * @param scheduledAt   预约时间
 * @param acceptedAt    接单时间
 * @param startedAt     开始时间
 * @param submittedAt   提交验收时间
 * @param completedAt   完成时间
 * @param cancelledAt   取消时间
 * @param cancelReason  取消原因
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 */
public record WorkOrder(
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
