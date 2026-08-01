package com.platformcommons.identity.domain;

import com.platformcommons.common.enums.StakeholderType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员角色枚举。
 *
 * <p>与 {@link StakeholderType} 一一对应，用于身份模块内部表达成员承担的角色。
 * 一个成员可同时持有多个角色。</p>
 */
@AllArgsConstructor
@Getter
public enum MemberRole {

    WORKER("劳动者", StakeholderType.WORKER),
    CONSUMER("消费者", StakeholderType.CONSUMER),
    MERCHANT("商户", StakeholderType.MERCHANT),
    PUBLIC_MEMBER("公共成员", StakeholderType.PUBLIC_MEMBER);

    private final String description;
    private final StakeholderType stakeholderType;

}