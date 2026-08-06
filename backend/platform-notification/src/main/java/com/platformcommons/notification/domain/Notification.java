package com.platformcommons.notification.domain;

import java.time.Instant;
import java.util.List;

/**
 * 通知领域模型（不可变 record）。
 *
 * @param id            主键
 * @param recipientId   接收人 ID
 * @param recipientRole 接收人角色（MERCHANT / WORKER / MEMBER / ADMIN）
 * @param category      通知分类
 * @param title         标题
 * @param content       内容
 * @param refType       关联业务类型（WorkOrder / PaymentOrder / Proposal 等）
 * @param refId         关联业务 ID
 * @param channels      投递渠道列表
 * @param status        投递状态
 * @param readAt        已读时间
 * @param createdAt     创建时间
 * @param sentAt        发送时间
 */
public record Notification(
        Long id,
        Long recipientId,
        String recipientRole,
        NotificationCategory category,
        String title,
        String content,
        String refType,
        String refId,
        List<NotificationChannel> channels,
        NotificationStatus status,
        Instant readAt,
        Instant createdAt,
        Instant sentAt
) {
}
