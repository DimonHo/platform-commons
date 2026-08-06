package com.platformcommons.payment.domain.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 交易 Repository。
 */
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    /**
     * 按订单号查询交易（幂等检查）。
     *
     * @param orderId 订单号
     * @return 交易（可能不存在）
     */
    Optional<TransactionEntity> findByOrderId(String orderId);
}
