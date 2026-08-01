package com.platformcommons.finance.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 融资审查请求 DTO
 *
 * @param amount        融资金额
 * @param financingType 融资类型
 * @param repaymentCap  偿付上限
 * @param noGovernance  无治理权声明
 */
public record FinancingReviewRequest(
        @NotNull(message = "融资金额不能为空")
        @Positive(message = "融资金额必须为正数")
        BigDecimal amount,

        @NotNull(message = "融资类型不能为空")
        String financingType,

        @NotNull(message = "偿付上限不能为空")
        @Positive(message = "偿付上限必须为正数")
        BigDecimal repaymentCap,

        boolean noGovernance
) implements Serializable {}
