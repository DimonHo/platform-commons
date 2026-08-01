package com.platformcommons.dispute.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 争议状态（第15章 第93-96条）
 */
@Getter
@AllArgsConstructor
public enum DisputeStatus {

    /** 已提交，待处理 */
    FILED("已提交"),

    /** 审查中 */
    UNDER_REVIEW("审查中"),

    /** 已裁决 */
    RESOLVED("已裁决"),

    /** 已上诉（升级到下一级） */
    APPEALED("已上诉"),

    /** 已撤回 */
    WITHDRAWN("已撤回"),

    /** 已结案（外部渠道） */
    CLOSED("已结案");

    private final String description;

}