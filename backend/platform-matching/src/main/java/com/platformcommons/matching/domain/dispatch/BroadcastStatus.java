package com.platformcommons.matching.domain.dispatch;

import lombok.Getter;

/**
 * 广播状态。
 */
@Getter
public enum BroadcastStatus {

    BROADCASTING("广播中"),
    GRABBED("已抢到"),
    EXPIRED("已过期"),
    CANCELLED("已取消");

    private final String description;

    BroadcastStatus(String description) {
        this.description = description;
    }
}
