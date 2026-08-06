package com.platformcommons.payment.repository;

import com.platformcommons.payment.repository.entity.BankCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 银行卡仓储。
 */
@Repository
public interface BankCardRepository extends JpaRepository<BankCardEntity, Long> {

    /**
     * 按会员 ID 查询全部银行卡。
     *
     * @param memberId 会员 ID
     * @return 银行卡列表
     */
    List<BankCardEntity> findByMemberId(Long memberId);

    /**
     * 按会员 ID 查询默认银行卡。
     *
     * @param memberId 会员 ID
     * @return 默认银行卡（可能为空）
     */
    Optional<BankCardEntity> findByMemberIdAndIsDefaultTrue(Long memberId);
}
