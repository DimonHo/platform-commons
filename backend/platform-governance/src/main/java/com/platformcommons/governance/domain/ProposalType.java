package com.platformcommons.governance.domain;

/**
 * 提案类型枚举。
 */
public enum ProposalType {

    POLICY_CHANGE("政策变更"),
    SETTLEMENT_RULE("结算规则"),
    BUDGET_ALLOCATION("预算分配"),
    CHARTER_AMENDMENT("章程修订"),
    OTHER("其他");

    private final String displayName;

    ProposalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
