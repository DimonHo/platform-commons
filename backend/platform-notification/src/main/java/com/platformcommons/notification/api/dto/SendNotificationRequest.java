package com.platformcommons.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送通知请求 DTO。
 *
 * @param recipientId   接收人 ID
 * @param recipientRole 接收人角色
 * @param category      通知分类（ORDER / PAYMENT / GOVERNANCE / SYSTEM）
 * @param title         标题
 * @param content       内容
 * @param refType       关联业务类型
 * @param refId         关联业务 ID
 * @param channels      投递渠道（如 IN_APP,SMS），默认 IN_APP
 */
public record SendNotificationRequest(
        @NotNull Long recipientId,
        String recipientRole,
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content,
        String refType,
        String refId,
        String channels
) {
}
