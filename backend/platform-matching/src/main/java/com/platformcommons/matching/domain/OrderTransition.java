package com.platformcommons.matching.domain;

import java.time.Instant;

/**
 * 工单状态流转记录领域模型（不可变）。
 *
 * @param id             主键
 * @param orderId        工单 ID
 * @param fromStatus     原状态
 * @param toStatus       目标状态
 * @param action         流转动作
 * @param operatorId     操作人 ID
 * @param operatorRole   操作人角色
 * @param remark         备注
 * @param attachmentUrls 附件 URL（JSON 数组字符串）
 * @param createdAt      创建时间
 */
public record OrderTransition(
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
