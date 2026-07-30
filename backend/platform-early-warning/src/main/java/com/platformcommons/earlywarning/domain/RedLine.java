package com.platformcommons.earlywarning.domain;

/**
 * 红线规则标识枚举。
 *
 * <p>映射宪章第16章 100条：列出所有五类红线，触发任一即自动启动应急措施。
 * 解除必须经监察委员会确认。
 */
public enum RedLine {

    /** 红线1：单一外部资本持股超过 20%。 */
    SINGLE_CAPITAL_CONCENTRATION("R-CAP-01", "单一外部资本持股超过20%", AlertCategory.FINANCE_AND_CAPITAL),

    /** 红线2：劳动者净收入占比低于 70%（反榨取底线突破）。 */
    WORKER_INCOME_BELOW_FLOOR("R-LAB-01", "劳动者净收入占比低于70%反榨取底线", AlertCategory.ORGANIZATION_AND_LABOR),

    /** 红线3：核心算法不开源或不可审计。 */
    ALGORITHM_NOT_AUDITABLE("R-TECH-01", "核心算法不开源或不可审计", AlertCategory.TECHNICAL),

    /** 红线4：治理结构被外部实体实际控制。 */
    GOVERNANCE_CAPTURED("R-GOV-01", "治理结构被外部实体实际控制", AlertCategory.TAKEOVER_ATTEMPT),

    /** 红线5：紧急状态超过 14 天法定上限。 */
    EMERGENCY_OVER_LIMIT("R-EMG-01", "紧急状态超过14天法定上限", AlertCategory.ORGANIZATION_AND_LABOR);

    private final String code;
    private final String description;
    private final AlertCategory category;

    RedLine(String code, String description, AlertCategory category) {
        this.code = code;
        this.description = description;
        this.category = category;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public AlertCategory category() {
        return category;
    }
}
