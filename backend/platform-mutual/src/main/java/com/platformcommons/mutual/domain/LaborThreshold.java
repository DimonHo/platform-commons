package com.platformcommons.mutual.domain;

import java.math.BigDecimal;

/**
 * 月度劳动门槛。
 *
 * <p>映射宪章第14章 83条：不同工种设定不同的劳动门槛，作为互助保障资格的准入条件。
 *
 * @param jobCategory   工种类别（如 "delivery"、"care"、"craft"）
 * @param h0            月度最低劳动时长（小时）
 * @param q0            月度最低质量分（0-100）
 * @param d0            月度最低贡献度（0-100）
 */
public record LaborThreshold(
        String jobCategory,
        BigDecimal h0,
        BigDecimal q0,
        BigDecimal d0
) {

    /** 默认基础保障门槛：每月 40 小时。 */
    public static final BigDecimal DEFAULT_H0 = new BigDecimal("40");
    /** 默认质量分门槛：60。 */
    public static final BigDecimal DEFAULT_Q0 = new BigDecimal("60");
    /** 默认贡献度门槛：50。 */
    public static final BigDecimal DEFAULT_D0 = new BigDecimal("50");

    /** 配送工种门槛。 */
    public static final LaborThreshold DELIVERY = new LaborThreshold(
            "delivery", DEFAULT_H0, DEFAULT_Q0, DEFAULT_D0);

    /** 护理工种门槛。 */
    public static final LaborThreshold CARE = new LaborThreshold(
            "care", new BigDecimal("30"), DEFAULT_Q0, DEFAULT_D0);

    /** 手艺工种门槛。 */
    public static final LaborThreshold CRAFT = new LaborThreshold(
            "craft", new BigDecimal("20"), DEFAULT_Q0, DEFAULT_D0);
}
