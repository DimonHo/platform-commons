package com.platformcommons.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知分类。
 */
@Getter
@AllArgsConstructor
public enum NotificationCategory {
    ORDER("订单"),
    PAYMENT("支付"),
    GOVERNANCE("治理"),
    SYSTEM("系统");
    private final String description;
}
