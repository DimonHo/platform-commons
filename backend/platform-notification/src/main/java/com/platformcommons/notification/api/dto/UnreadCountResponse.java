package com.platformcommons.notification.api.dto;

/**
 * 未读通知计数 DTO。
 *
 * @param recipientId 接收人 ID
 * @param unreadCount 未读数
 */
public record UnreadCountResponse(
        Long recipientId,
        long unreadCount
) {
}
