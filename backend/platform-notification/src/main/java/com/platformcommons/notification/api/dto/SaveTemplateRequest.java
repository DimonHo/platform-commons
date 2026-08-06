package com.platformcommons.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建/更新通知模板请求 DTO。
 *
 * @param code             模板编码
 * @param name             模板名称
 * @param category         通知分类
 * @param titleTemplate    标题模板
 * @param contentTemplate  内容模板
 * @param defaultChannels  默认投递渠道
 * @param enabled          是否启用
 */
public record SaveTemplateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String titleTemplate,
        @NotBlank String contentTemplate,
        @NotBlank String defaultChannels,
        Boolean enabled
) {
}
