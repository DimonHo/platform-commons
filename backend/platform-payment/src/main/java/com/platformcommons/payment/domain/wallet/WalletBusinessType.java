package com.platformcommons.payment.domain.wallet;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包业务类型。
 */
@Getter
@AllArgsConstructor
public enum WalletBusinessType {

    RECHARGE("充值"),
    WITHDRAW("提现"),
    REFUND("退款"),
    MUTUAL_CLAIM("互助理赔"),
    SETTLE("分账");

    private final String description;
}
