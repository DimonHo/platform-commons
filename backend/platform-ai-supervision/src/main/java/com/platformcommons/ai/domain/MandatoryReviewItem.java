package com.platformcommons.ai.domain;

/**
 * 强制审议事项清单（第62条）
 * <p>
 * 以下事项必须经过 AI 多角色交叉审议方可执行，
 * 任何绕过审议的决策视为治理违规。
 */
public enum MandatoryReviewItem {

    /** 定价算法变更 */
    PRICING_ALGORITHM_CHANGE("定价算法变更"),

    /** 劳动者匹配规则变更 */
    MATCHING_RULE_CHANGE("劳动者匹配规则变更"),

    /** 平台服务费率调整 */
    SERVICE_FEE_ADJUSTMENT("平台服务费率调整"),

    /** 用户数据使用策略变更 */
    DATA_USAGE_POLICY_CHANGE("用户数据使用策略变更"),

    /** 自动化处罚规则变更 */
    AUTOMATED_PENALTY_RULE_CHANGE("自动化处罚规则变更"),

    /** 信用评分模型变更 */
    CREDIT_SCORING_MODEL_CHANGE("信用评分模型变更"),

    /** 调度优先级规则变更 */
    DISPATCH_PRIORITY_RULE_CHANGE("调度优先级规则变更"),

    /** 算法权重参数调整 */
    ALGORITHM_WEIGHT_ADJUSTMENT("算法权重参数调整"),

    /** 新增自动化决策场景 */
    NEW_AUTOMATED_DECISION_SCENARIO("新增自动化决策场景"),

    /** 现有自动化决策场景移除 */
    REMOVAL_OF_AUTOMATED_DECISION("移除自动化决策场景");

    private final String description;

    MandatoryReviewItem(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
