package com.platformcommons.payment.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 收款请求 DTO。
 *
 * @param orderId     订单 ID
 * @param workerId    劳动者 ID
 * @param requesterId 发包方 ID
 * @param grossAmount 订单总价（必须大于 0）
 */
public record ChargeRequest(
        @NotBlank(message = "orderId must not be blank")
        String orderId,

        @NotBlank(message = "workerId must not be blank")
        String workerId,

        @NotBlank(message = "requesterId must not be blank")
        String requesterId,

        @NotNull(message = "grossAmount must not be null")
        @DecimalMin(value = "0.01", message = "grossAmount must be >= 0.01")
        BigDecimal grossAmount
) {
}
