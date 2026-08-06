package com.platformcommons.matching.domain;

import lombok.Getter;

/**
 * 工单状态。
 */
@Getter
public enum WorkOrderStatus {

    CREATED("已创建"),
    DISPATCHED("已派单"),
    ACCEPTED("已接单"),
    IN_PROGRESS("进行中"),
    SUBMITTED("已提交验收"),
    APPROVED("验收通过"),
    REJECTED("验收驳回"),
    SETTLED("已结算"),
    CLOSED("已关闭"),
    CANCELLED("已取消"),
    DISPUTED("争议中");

    private final String description;

    WorkOrderStatus(String description) {
        this.description = description;
    }
}
