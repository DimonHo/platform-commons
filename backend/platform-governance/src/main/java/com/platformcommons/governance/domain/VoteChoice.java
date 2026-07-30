package com.platformcommons.governance.domain;

/**
 * 投票选择枚举。
 */
public enum VoteChoice {

    YES("赞成"),
    NO("反对"),
    ABSTAIN("弃权");

    private final String displayName;

    VoteChoice(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
