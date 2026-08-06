package com.platformcommons.payment.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道编码。
 */
@Getter
@AllArgsConstructor
public enum ChannelCode {

    WECHAT_PAY("微信支付"),
    ALIPAY("支付宝"),
    UNIONPAY("银联"),
    BANK_TRANSFER("银行转账");

    private final String description;
}
