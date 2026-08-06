package com.platformcommons.payment.domain.withdrawal;

import com.platformcommons.payment.domain.withdrawal.WithdrawalRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 提现申请仓储。
 */
@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequestEntity, Long> {

    /**
     * 按申请单号查询提现申请。
     *
     * @param requestNo 申请单号
     * @return 提现申请实体（可能为空）
     */
    Optional<WithdrawalRequestEntity> findByRequestNo(String requestNo);

    /**
     * 按会员 ID 查询提现申请（按申请时间倒序）。
     *
     * @param memberId 会员 ID
     * @return 提现申请列表
     */
    List<WithdrawalRequestEntity> findByMemberIdOrderByAppliedAtDesc(Long memberId);
}
