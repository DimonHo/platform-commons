package com.platformcommons.identity.api.dto;

import com.platformcommons.common.enums.MemberStatus;
import com.platformcommons.common.mask.Mask;
import com.platformcommons.common.mask.MaskType;
import com.platformcommons.identity.domain.role.MemberRole;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 成员响应 DTO（对外脱敏后视图）。
 *
 * @param id           成员 ID
 * @param name         姓名
 * @param phone        手机号（原始值，序列化时由 {@code @Mask} 自动脱敏）
 * @param roles        角色集合
 * @param registeredAt 注册时间
 * @param status       状态
 * @param laborShares  劳动份额
 */
public record MemberResponse(
        Long id,
        String name,
        @Mask(MaskType.PHONE) String phone,
        Set<MemberRole> roles,
        LocalDateTime registeredAt,
        MemberStatus status,
        Integer laborShares
) {
}
