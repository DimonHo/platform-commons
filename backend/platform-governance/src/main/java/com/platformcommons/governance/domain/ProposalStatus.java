package com.platformcommons.governance.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提案状态枚举。
 */
@AllArgsConstructor
@Getter
public enum ProposalStatus {

    DRAFT("草稿"),
    OPEN("投票中"),
    PASSED("通过"),
    REJECTED("否决"),
    WITHDRAWN("撤回"),
    CLOSED("已关闭");

    private final String description;

}