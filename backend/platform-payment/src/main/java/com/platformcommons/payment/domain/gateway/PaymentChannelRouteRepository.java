package com.platformcommons.payment.domain.gateway;

import com.platformcommons.payment.domain.gateway.PaymentChannelRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付渠道路由仓储。
 */
@Repository
public interface PaymentChannelRouteRepository extends JpaRepository<PaymentChannelRouteEntity, Long> {

    /**
     * 按支付订单 ID 查询渠道路由列表。
     *
     * @param paymentOrderId 支付订单 ID
     * @return 路由列表
     */
    List<PaymentChannelRouteEntity> findByPaymentOrderId(Long paymentOrderId);
}
