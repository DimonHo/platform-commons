package com.platformcommons.payment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单方向。
 */
@Getter
@AllArgsConstructor
public enum PaymentOrderDirection {

    PAY("支付"),
    RECHARGE("充值");

    private final String description;
}
