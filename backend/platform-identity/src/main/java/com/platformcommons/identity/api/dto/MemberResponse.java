package com.platformcommons.identity.api.dto;

import com.platformcommons.identity.domain.MemberRole;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 成员响应 DTO（对外脱敏后视图）。
 *
 * @param id           成员 ID
 * @param name         姓名
 * @param phone        脱敏手机号
 * @param roles        角色集合
 * @param registeredAt 注册时间
 * @param status       状态
 * @param laborShares  劳动份额
 */
public record MemberResponse(
        Long id,
        String name,
        String phone,
        Set<MemberRole> roles,
        LocalDateTime registeredAt,
        String status,
        Integer laborShares
) {
}
