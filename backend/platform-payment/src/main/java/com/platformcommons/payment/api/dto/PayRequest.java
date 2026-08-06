package com.platformcommons.payment.api.dto;

import com.platformcommons.payment.domain.ChannelCode;
import com.platformcommons.payment.domain.PaymentOrderDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * 支付请求。
 *
 * @param memberId     会员 ID
 * @param direction    订单方向
 * @param amount       金额
 * @param businessType 业务类型
 * @param refType      关联业务类型
 * @param refId        关联业务 ID
 * @param channel      支付渠道
 */
public record PayRequest(
        @NotNull(message = "会员 ID 不能为空")
        Long memberId,

        @NotNull(message = "订单方向不能为空")
        PaymentOrderDirection direction,

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须为正数")
        BigDecimal amount,

        @NotBlank(message = "业务类型不能为空")
        String businessType,

        String refType,

        String refId,

        @NotNull(message = "支付渠道不能为空")
        ChannelCode channel
) {
}
