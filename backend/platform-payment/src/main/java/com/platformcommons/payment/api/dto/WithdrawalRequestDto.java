package com.platformcommons.payment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 提现请求。
 *
 * @param memberId   会员 ID
 * @param bankCardId 银行卡 ID
 * @param amount     提现金额（必须为正）
 */
public record WithdrawalRequestDto(
        @NotNull(message = "会员 ID 不能为空")
        Long memberId,

        @NotNull(message = "银行卡 ID 不能为空")
        Long bankCardId,

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须为正数")
        BigDecimal amount
) {
}
