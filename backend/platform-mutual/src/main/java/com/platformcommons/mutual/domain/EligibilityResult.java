package com.platformcommons.mutual.domain;

/**
 * 资格认定结果。
 *
 * <p>映射宪章第14章 83-87条：
 * <ul>
 *   <li>baseGuaranteed：基础保障（满足 H0 月度劳动门槛即享）。</li>
 *   <li>enhancedEligible：增强保障（满足 Q0 质量分 + D0 贡献度门槛）。</li>
 * </ul>
 *
 * @param applicantId        申请人 ID
 * @param baseGuaranteed     基础保障资格
 * @param enhancedEligible   增强保障资格
 * @param h0Satisfied        月度劳动时长门槛 H0 是否满足
 * @param q0Satisfied        质量分门槛 Q0 是否满足
 * @param d0Satisfied        贡献度门槛 D0 是否满足
 * @param eligibleAmount     可享保障金额上限
 * @param reason             不满足原因（可空）
 */
public record EligibilityResult(
        String applicantId,
        boolean baseGuaranteed,
        boolean enhancedEligible,
        boolean h0Satisfied,
        boolean q0Satisfied,
        boolean d0Satisfied,
        java.math.BigDecimal eligibleAmount,
        String reason
) {
}
