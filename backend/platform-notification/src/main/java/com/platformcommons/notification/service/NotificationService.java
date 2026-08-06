package com.platformcommons.notification.service;

import com.platformcommons.notification.domain.Notification;
import com.platformcommons.notification.domain.NotificationCategory;
import com.platformcommons.notification.domain.NotificationChannel;
import com.platformcommons.notification.domain.NotificationStatus;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口。
 */
public interface NotificationService {

    /**
     * 直接发送通知（无模板）。
     */
    Notification send(Long recipientId, String recipientRole, NotificationCategory category,
                      String title, String content, String refType, String refId,
                      List<NotificationChannel> channels);

    /**
     * 基于模板发送通知，占位符自动填充。
     */
    Notification sendByTemplate(String templateCode, Long recipientId, String recipientRole,
                                String refType, String refId, Map<String, String> placeholders,
                                List<NotificationChannel> channels);

    /**
     * 查询用户通知列表（分页）。
     */
    List<Notification> listByRecipient(Long recipientId, int page, int size);

    /**
     * 查询未读通知。
     */
    List<Notification> listUnread(Long recipientId);

    /**
     * 标记为已读。
     */
    Notification markAsRead(Long notificationId);

    /**
     * 批量标记为已读。
     */
    int markAllRead(Long recipientId);

    /**
     * 未读计数。
     */
    long countUnread(Long recipientId);

    /**
     * 按分类查询。
     */
    List<Notification> listByCategory(NotificationCategory category);
}
