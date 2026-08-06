package com.platformcommons.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 绑卡请求。
 *
 * @param holderName    持卡人姓名
 * @param cardNo        卡号（明文）
 * @param bankName      开户行
 * @param cardType      卡类型：DEBIT / CREDIT
 * @param reservedPhone 银行预留手机号
 */
public record BindCardRequest(
        @NotNull(message = "会员 ID 不能为空")
        Long memberId,

        @NotBlank(message = "持卡人姓名不能为空")
        String holderName,

        @NotBlank(message = "卡号不能为空")
        String cardNo,

        @NotBlank(message = "开户行不能为空")
        String bankName,

        @NotBlank(message = "卡类型不能为空")
        String cardType,

        String reservedPhone
) {
}
