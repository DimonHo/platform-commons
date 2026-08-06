package com.platformcommons.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 投递渠道。
 */
@Getter
@AllArgsConstructor
public enum NotificationChannel {
    IN_APP("站内"),
    SMS("短信"),
    PUSH("推送");
    private final String description;
}
