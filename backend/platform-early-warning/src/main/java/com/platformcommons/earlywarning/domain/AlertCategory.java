package com.platformcommons.earlywarning.domain;

/**
 * 预警类别枚举。
 *
 * <p>映射宪章第16章 98条：五类异化风险领域（技术类拆为技术与接管两类）。
 */
public enum AlertCategory {

    /** 组织与劳动关系异化。 */
    ORGANIZATION_AND_LABOR,

    /** 财务与资本异化。 */
    FINANCE_AND_CAPITAL,

    /** 技术异化（算法黑箱、数据垄断）。 */
    TECHNICAL,

    /** 外部接管企图（资本收购、政治俘获）。 */
    TAKEOVER_ATTEMPT
}
