package com.platformcommons.governance.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 投票选择枚举。
 */
@Getter
@AllArgsConstructor
public enum VoteChoice {

    YES("赞成"),
    NO("反对"),
    ABSTAIN("弃权");

    private final String displayName;

}