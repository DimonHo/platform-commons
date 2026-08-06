package com.platformcommons.payment.domain;

import java.time.Instant;

/**
 * 银行卡领域模型（不可变记录）。
 *
 * @param id            银行卡 ID
 * @param memberId      会员 ID
 * @param holderName    持卡人姓名
 * @param cardNoEnc     卡号密文（演示用反转字符串）
 * @param cardNoMasked  卡号掩码（**** + 末四位）
 * @param bankName      开户行
 * @param cardType      卡类型：DEBIT / CREDIT
 * @param isDefault     是否默认卡
 * @param status        卡状态
 * @param boundAt       绑定时间
 */
public record BankCard(
        Long id,
        Long memberId,
        String holderName,
        String cardNoEnc,
        String cardNoMasked,
        String bankName,
        String cardType,
        Boolean isDefault,
        CardStatus status,
        Instant boundAt
) {
}
