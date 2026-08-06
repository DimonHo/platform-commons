package com.platformcommons.matching.domain;

import lombok.Getter;

/**
 * 抢单结果状态。
 */
@Getter
public enum GrabStatus {

    PENDING("待定"),
    WIN("抢到"),
    LOSE("未抢到");

    private final String description;

    GrabStatus(String description) {
        this.description = description;
    }
}
