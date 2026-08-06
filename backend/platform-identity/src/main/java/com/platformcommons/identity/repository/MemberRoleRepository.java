package com.platformcommons.identity.repository;

import com.platformcommons.identity.domain.RoleType;
import com.platformcommons.identity.repository.entity.MemberRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 成员角色 Repository。
 */
public interface MemberRoleRepository extends JpaRepository<MemberRoleEntity, Long> {

    /**
     * 根据成员 ID 与角色类型查询角色记录。
     */
    Optional<MemberRoleEntity> findByMemberIdAndRoleType(Long memberId, RoleType roleType);

    /**
     * 根据成员 ID 查询全部角色记录。
     */
    List<MemberRoleEntity> findByMemberId(Long memberId);
}
