package com.platformcommons.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * 基于模板发送通知请求 DTO。
 *
 * @param templateCode 模板编码
 * @param recipientId  接收人 ID
 * @param recipientRole 接收人角色
 * @param refType      关联业务类型
 * @param refId        关联业务 ID
 * @param placeholders 占位符键值对（如 orderNo / amount）
 * @param channels     覆盖渠道（为空则使用模板默认渠道）
 */
public record SendTemplatedNotificationRequest(
        @NotBlank String templateCode,
        @NotNull Long recipientId,
        String recipientRole,
        String refType,
        String refId,
        Map<String, String> placeholders,
        String channels
) {
}
