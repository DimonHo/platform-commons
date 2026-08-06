package com.platformcommons.payment.repository;

import com.platformcommons.payment.repository.entity.WithdrawalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 提现记录仓储。
 */
@Repository
public interface WithdrawalRecordRepository extends JpaRepository<WithdrawalRecordEntity, Long> {

    /**
     * 按提现申请 ID 查询提现记录列表。
     *
     * @param withdrawalRequestId 提现申请 ID
     * @return 提现记录列表
     */
    List<WithdrawalRecordEntity> findByWithdrawalRequestId(Long withdrawalRequestId);
}
