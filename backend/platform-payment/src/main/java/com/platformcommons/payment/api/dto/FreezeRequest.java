package com.platformcommons.payment.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 冻结请求。
 *
 * @param memberId 会员 ID
 * @param amount   冻结金额
 * @param refType  关联业务类型
 * @param refId    关联业务 ID
 */
public record FreezeRequest(
        @NotNull(message = "会员 ID 不能为空")
        Long memberId,

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须为正数")
        BigDecimal amount,

        String refType,

        String refId
) {
}
