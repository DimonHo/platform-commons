package com.platformcommons.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包状态。
 */
@Getter
@AllArgsConstructor
public enum WalletStatus {

    ACTIVE("正常"),
    FROZEN("冻结"),
    CLOSED("已注销");

    private final String description;
}
