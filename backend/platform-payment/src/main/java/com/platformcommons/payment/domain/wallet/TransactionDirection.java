package com.platformcommons.payment.domain.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包收支方向。
 */
@Getter
@AllArgsConstructor
public enum TransactionDirection {

    IN("收入"),
    OUT("支出");

    private final String description;
}
