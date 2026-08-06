package com.platformcommons.matching.domain.workorder;

import lombok.Getter;

/**
 * 工单流转动作。
 */
@Getter
public enum TransitionAction {

    DISPATCH("派单"),
    ACCEPT("接单"),
    START("开始"),
    SUBMIT("提交验收"),
    APPROVE("验收通过"),
    REJECT("驳回"),
    CANCEL("取消"),
    DISPUTE("发起争议"),
    SETTLE("结算");

    private final String description;

    TransitionAction(String description) {
        this.description = description;
    }
}
