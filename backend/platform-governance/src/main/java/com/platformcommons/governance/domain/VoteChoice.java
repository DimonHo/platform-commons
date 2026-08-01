package com.platformcommons.governance.domain;

import lombok.Getter;

/**
 * 投票选择枚举。
 */
@Getter
public enum VoteChoice {

    YES("赞成"),
    NO("反对"),
    ABSTAIN("弃权");

    private final String displayName;

    VoteChoice(String displayName) {
        this.displayName = displayName;
    }
}
