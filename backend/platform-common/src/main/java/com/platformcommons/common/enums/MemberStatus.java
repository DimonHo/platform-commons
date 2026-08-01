package com.platformcommons.common.enums;

import lombok.Getter;

/**
 * 成员状态枚举。
 */
@Getter
public enum MemberStatus {

    ACTIVE("活跃"),
    SUSPENDED("暂停"),
    WITHDRAWN("退出");

    private final String description;

    MemberStatus(String description) {
        this.description = description;
    }
}
