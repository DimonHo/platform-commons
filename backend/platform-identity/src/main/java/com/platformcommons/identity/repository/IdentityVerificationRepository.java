package com.platformcommons.identity.repository;

import com.platformcommons.identity.repository.entity.IdentityVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 实名认证 Repository。
 */
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerificationEntity, Long> {

    /**
     * 根据成员 ID 查询实名认证记录。
     */
    Optional<IdentityVerificationEntity> findByMemberId(Long memberId);
}
