package com.platformcommons.payment.repository;

import com.platformcommons.payment.repository.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付订单仓储。
 */
@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, Long> {

    /**
     * 按订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 订单实体（可能为空）
     */
    Optional<PaymentOrderEntity> findByOrderNo(String orderNo);

    /**
     * 按会员 ID 查询订单（按创建时间倒序）。
     *
     * @param memberId 会员 ID
     * @return 订单列表
     */
    List<PaymentOrderEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
