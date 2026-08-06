package com.platformcommons.matching.domain;

import lombok.Getter;

/**
 * 工单类型。
 */
@Getter
public enum WorkOrderType {

    LABOR("劳务"),
    SERVICE("服务"),
    DELIVERY("配送"),
    RIDE_HAIL("网约出行"),
    MUTUAL_ASSIST("互助");

    private final String description;

    WorkOrderType(String description) {
        this.description = description;
    }
}
