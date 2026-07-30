package com.platformcommons.payment.repository;

import com.platformcommons.payment.repository.entity.LedgerEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 账本事件 Repository。
 */
public interface LedgerEventRepository extends JpaRepository<LedgerEventEntity, UUID> {

    /**
     * 按交易 ID 查询所有账本事件（按发生时间升序）。
     *
     * @param transactionId 交易 ID
     * @return 事件列表
     */
    List<LedgerEventEntity> findByTransactionIdOrderByOccurredAtAsc(UUID transactionId);
}
