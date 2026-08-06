package com.platformcommons.notification.domain;

import java.time.Instant;

/**
 * 通知模板领域模型（不可变 record）。
 *
 * @param id               主键
 * @param code             模板编码（如 ORDER_CREATED_MERCHANT）
 * @param name             模板名称
 * @param category         通知分类
 * @param titleTemplate    标题模板（含占位符）
 * @param contentTemplate  内容模板（含占位符）
 * @param defaultChannels  默认投递渠道
 * @param enabled          是否启用
 * @param createdAt        创建时间
 */
public record NotificationTemplate(
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
