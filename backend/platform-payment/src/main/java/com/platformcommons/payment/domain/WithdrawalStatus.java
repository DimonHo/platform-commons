package com.platformcommons.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现状态。
 */
@Getter
@AllArgsConstructor
public enum WithdrawalStatus {

    PENDING("待审核"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    PROCESSING("处理中"),
    SUCCESS("成功"),
    FAILED("失败");

    private final String description;
}
