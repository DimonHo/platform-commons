package com.platformcommons.payment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 充值请求。
 *
 * @param memberId 会员 ID
 * @param amount   充值金额（必须为正）
 */
public record RechargeRequest(
        @NotNull(message = "会员 ID 不能为空")
        Long memberId,

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须为正数")
        BigDecimal amount
) {
}
