package com.platformcommons.payment.domain.transaction;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 按 ID 悲观锁查询（PG 行锁），用于结算/退款等读-改-写链路，防止并发双重结算导致钱包重复扣款/入账。
     *
     * @param id 交易 ID
     * @return 交易（可能不存在）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransactionEntity t where t.id = :id")
    Optional<TransactionEntity> findByIdForUpdate(@Param("id") UUID id);
}
