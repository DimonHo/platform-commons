package com.platformcommons.identity.domain.profile;

import com.platformcommons.identity.domain.profile.MerchantProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 商家档案 Repository。
 */
public interface MerchantProfileRepository extends JpaRepository<MerchantProfileEntity, Long> {

    /**
     * 根据成员 ID 查询商家档案。
     */
    Optional<MerchantProfileEntity> findByMemberId(Long memberId);
}
