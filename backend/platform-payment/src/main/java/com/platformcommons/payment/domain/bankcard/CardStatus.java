package com.platformcommons.payment.domain.bankcard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 银行卡状态。
 */
@Getter
@AllArgsConstructor
public enum CardStatus {

    ACTIVE("正常"),
    UNBOUND("已解绑");

    private final String description;
}
