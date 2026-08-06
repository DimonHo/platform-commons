package com.platformcommons.payment.repository;

import com.platformcommons.payment.repository.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 钱包仓储。
 */
@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    /**
     * 按会员 ID 查询钱包。
     *
     * @param memberId 会员 ID
     * @return 钱包实体（可能为空）
     */
    Optional<WalletEntity> findByMemberId(Long memberId);
}
