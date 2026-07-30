package com.platformcommons.mutual.api.dto;

import java.math.BigDecimal;

/**
 * 资格认定响应 DTO。
 *
 * @param applicantId      申请人 ID
 * @param baseGuaranteed   基础保障资格
 * @param enhancedEligible 增强保障资格
 * @param h0Satisfied      H0 门槛满足
 * @param q0Satisfied      Q0 门槛满足
 * @param d0Satisfied      D0 门槛满足
 * @param eligibleAmount   可享保障金额上限
 * @param reason           不满足原因
 */
public record EligibilityResponse(
        String applicantId,
        boolean baseGuaranteed,
        boolean enhancedEligible,
        boolean h0Satisfied,
        boolean q0Satisfied,
        boolean d0Satisfied,
        BigDecimal eligibleAmount,
        String reason
) {
}
