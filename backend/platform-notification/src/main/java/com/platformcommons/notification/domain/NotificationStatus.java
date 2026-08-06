package com.platformcommons.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知投递状态。
 */
@Getter
@AllArgsConstructor
public enum NotificationStatus {
    PENDING("待发送"),
    SENT("已发送"),
    DELIVERED("已送达"),
    READ("已读"),
    FAILED("发送失败");
    private final String description;
}
