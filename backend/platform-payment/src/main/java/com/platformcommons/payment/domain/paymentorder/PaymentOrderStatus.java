package com.platformcommons.payment.domain.paymentorder;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单状态。
 */
@Getter
@AllArgsConstructor
public enum PaymentOrderStatus {

    PENDING("待支付"),
    PAID("已支付"),
    FAILED("支付失败"),
    REFUNDED("已退款");

    private final String description;
}
