package com.platformcommons.dispute.domain;

import lombok.Getter;

/**
 * 争议救济层级（第15章 第93-96条）
 * <p>
 * 三级递进救济流程：先由业务团队复核，
 * 不满意可上诉至申诉委员会，仍不满意可引入外部调解/仲裁。
 */
@Getter
public enum DisputeLevel {

    /** 第一级：业务团队复核 */
    BUSINESS_REVIEW("业务团队复核"),

    /** 第二级：申诉委员会审议 */
    APPEAL_COMMITTEE("申诉委员会审议"),

    /** 第三级：外部调解/仲裁 */
    EXTERNAL("外部调解/仲裁");

    private final String description;

    DisputeLevel(String description) {
        this.description = description;
    }
}
