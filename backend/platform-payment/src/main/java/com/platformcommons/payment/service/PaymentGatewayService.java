package com.platformcommons.payment.service;

import com.platformcommons.payment.domain.ChannelCode;
import com.platformcommons.payment.domain.PaymentChannelRoute;
import com.platformcommons.payment.domain.PaymentOrder;
import com.platformcommons.payment.domain.PaymentOrderDirection;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统一支付网关服务接口。
 */
public interface PaymentGatewayService {

    /**
     * 创建支付订单。
     *
     * @param memberId     会员 ID
     * @param direction    订单方向
     * @param amount       金额
     * @param businessType 业务类型
     * @param refType      关联业务类型
     * @param refId        关联业务 ID
     * @return 支付订单
     */
    PaymentOrder createPaymentOrder(Long memberId, PaymentOrderDirection direction, BigDecimal amount, String businessType, String refType, String refId);

    /**
     * 支付订单（指定渠道）。
     *
     * @param orderId 订单 ID
     * @param channel 支付渠道
     * @return 渠道路由
     */
    PaymentChannelRoute payOrder(Long orderId, ChannelCode channel);

    /**
     * 按订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 支付订单
     */
    PaymentOrder getPaymentOrder(String orderNo);

    /**
     * 按会员 ID 查询订单列表。
     *
     * @param memberId 会员 ID
     * @return 订单列表
     */
    List<PaymentOrder> listMemberOrders(Long memberId);
}
