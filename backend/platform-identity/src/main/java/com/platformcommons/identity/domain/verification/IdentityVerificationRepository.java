package com.platformcommons.identity.domain.verification;

import com.platformcommons.identity.domain.verification.IdentityVerificationEntity;
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
