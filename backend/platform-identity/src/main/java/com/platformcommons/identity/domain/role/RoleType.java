package com.platformcommons.identity.domain.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员多身份角色类型枚举。
 */
@Getter
@AllArgsConstructor
public enum RoleType {
    MEMBER("普通成员"), WORKER("劳动者"), MERCHANT("商家"), ADMIN("管理员"), REVIEWER("审核员");

    private final String description;
}
