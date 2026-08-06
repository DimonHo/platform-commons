package com.platformcommons.identity.domain.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 银行卡类型枚举。
 */
@Getter
@AllArgsConstructor
public enum CardType {
    DEBIT("借记卡"), CREDIT("信用卡");

    private final String description;
}
