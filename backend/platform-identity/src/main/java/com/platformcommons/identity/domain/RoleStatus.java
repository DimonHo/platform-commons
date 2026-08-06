package com.platformcommons.identity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色状态枚举。
 */
@Getter
@AllArgsConstructor
public enum RoleStatus {
    PENDING("待激活"), ACTIVE("正常"), SUSPENDED("暂停"), REVOKED("撤销");

    private final String description;
}
