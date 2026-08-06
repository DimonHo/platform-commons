package com.platformcommons.notification.api.dto;

import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationChannel;
import com.platformcommons.notification.domain.NotificationStatus;

import java.time.Instant;
import java.util.List;

/**
 * 通知响应 DTO。
 */
public record NotificationResponse(
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
