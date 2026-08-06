package com.platformcommons.identity.domain.member;

import com.platformcommons.common.enums.MemberStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 成员领域模型（不可变 record）。
 *
 * <p>聚合根，表达平台共同体的一名注册成员与其角色组合。
 * 领域层不依赖持久化细节。</p>
 *
 * @param id           成员唯一标识
 * @param name         姓名
 * @param phone        手机号
 * @param roles        角色集合（可同时为劳动者与消费者等）
 * @param registeredAt 注册时间
 * @param status       当前状态
 * @param laborShares  劳动份额（劳动者权益单位），非劳动者为 {@code null}
 */
public record Member(
        Long id,
        String name,
        String phone,
        Set<MemberRole> roles,
        LocalDateTime registeredAt,
        MemberStatus status,
        Integer laborShares
) {
}
