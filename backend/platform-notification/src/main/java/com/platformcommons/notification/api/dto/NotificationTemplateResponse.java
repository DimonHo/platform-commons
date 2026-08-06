package com.platformcommons.notification.api.dto;

import com.platformcommons.notification.domain.NotificationCategory;

import java.time.Instant;

/**
 * 通知模板响应 DTO。
 */
public record NotificationTemplateResponse(
        Long id,
        String code,
        String name,
        NotificationCategory category,
        String titleTemplate,
        String contentTemplate,
        String defaultChannels,
        Boolean enabled,
        Instant createdAt
) {
}
