package com.platformcommons.matching.domain.dispatch;

import lombok.Getter;

/**
 * 派单广播类型。
 */
@Getter
public enum BroadcastType {

    GRAB("抢单"),
    ASSIGN("系统指派");

    private final String description;

    BroadcastType(String description) {
        this.description = description;
    }
}
