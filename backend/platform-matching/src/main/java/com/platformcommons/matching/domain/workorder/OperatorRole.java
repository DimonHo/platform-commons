package com.platformcommons.matching.domain.workorder;

import lombok.Getter;

/**
 * 操作人角色。
 */
@Getter
public enum OperatorRole {

    MEMBER("需求方"),
    WORKER("劳动者"),
    ADMIN("管理员"),
    SYSTEM("系统");

    private final String description;

    OperatorRole(String description) {
        this.description = description;
    }
}
