package com.platformcommons.payment.domain.wallet;

import com.platformcommons.payment.domain.wallet.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包流水仓储。
 */
@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {

    /**
     * 按钱包 ID 查询流水（按创建时间倒序）。
     *
     * @param walletId 钱包 ID
     * @return 流水列表
     */
    List<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    /**
     * 按会员 ID 分页查询流水（按创建时间倒序）。
     *
     * @param memberId 会员 ID
     * @param pageable 分页参数
     * @return 流水分页
     */
    Page<WalletTransactionEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
