package com.platformcommons.identity.domain.role;

import java.time.Instant;

/**
 * 成员角色记录领域模型（不可变 record）。
 *
 * @param id            主键
 * @param memberId      成员 ID
 * @param roleType      角色类型
 * @param status        角色状态
 * @param appliedAt     申请时间
 * @param activatedAt   激活时间
 * @param suspendedAt   暂停时间
 * @param suspendReason 暂停原因
 * @param reviewerId    审核员 ID
 * @param createdAt     创建时间
 * @param updatedAt     更新时间
 */
public record MemberRoleRecord(
        Long id,
        Long memberId,
        RoleType roleType,
        RoleStatus status,
        Instant appliedAt,
        Instant activatedAt,
        Instant suspendedAt,
        String suspendReason,
        Long reviewerId,
        Instant createdAt,
        Instant updatedAt
) {
}
