package com.platformcommons.matching.domain.workorder;

import lombok.Getter;

/**
 * 工单优先级。
 */
@Getter
public enum OrderPriority {

    NORMAL("普通"),
    HIGH("高"),
    URGENT("紧急");

    private final String description;

    OrderPriority(String description) {
        this.description = description;
    }
}
