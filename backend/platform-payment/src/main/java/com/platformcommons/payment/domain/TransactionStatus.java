package com.platformcommons.payment.domain;

/**
 * 交易状态枚举。
 *
 * <p>阿里规范：枚举名 UpperCamelCase，枚举值全大写下划线。
 */
public enum TransactionStatus {

    /** 已创建（待支付）。 */
    PENDING,

    /** 已收款（已分账入账）。 */
    CHARGED,

    /** 已结算完成（劳动者返还已到账）。 */
    SETTLED,

    /** 已退款。 */
    REFUNDED,

    /** 已失败。 */
    FAILED
}
