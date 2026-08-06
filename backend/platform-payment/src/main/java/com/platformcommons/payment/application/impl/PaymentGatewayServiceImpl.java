package com.platformcommons.payment.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.payment.domain.gateway.ChannelCode;
import com.platformcommons.payment.domain.gateway.ChannelRouteStatus;
import com.platformcommons.payment.domain.gateway.PaymentChannelRoute;
import com.platformcommons.payment.domain.paymentorder.PaymentOrder;
import com.platformcommons.payment.domain.paymentorder.PaymentOrderDirection;
import com.platformcommons.payment.domain.paymentorder.PaymentOrderStatus;
import com.platformcommons.payment.domain.gateway.PaymentChannelRouteRepository;
import com.platformcommons.payment.domain.paymentorder.PaymentOrderRepository;
import com.platformcommons.payment.domain.gateway.PaymentChannelRouteEntity;
import com.platformcommons.payment.domain.paymentorder.PaymentOrderEntity;
import com.platformcommons.payment.application.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 统一支付网关服务实现。
 *
 * <p>阿里规范：金额比较必须使用 {@link BigDecimal#compareTo(Object)}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    /** 订单过期时长：30 分钟。 */
    private static final Duration ORDER_TTL = Duration.ofMinutes(30);

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentChannelRouteRepository paymentChannelRouteRepository;

    @Override
    public PaymentOrder createPaymentOrder(Long memberId, PaymentOrderDirection direction, BigDecimal amount,
                                           String businessType, String refType, String refId) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "订单金额必须为正数");
        }
        Instant now = Instant.now();
        PaymentOrderEntity entity = new PaymentOrderEntity();
        entity.setOrderNo(SnowflakeUtils.nextId("PAY"));
        entity.setMemberId(memberId);
        entity.setDirection(direction);
        entity.setAmount(amount);
        entity.setBusinessType(businessType);
        entity.setRefType(refType);
        entity.setRefId(refId);
        entity.setStatus(PaymentOrderStatus.PENDING);
        entity.setExpireAt(now.plus(ORDER_TTL));
        entity.setCreatedAt(now);
        paymentOrderRepository.save(entity);
        log.info("Payment order created: orderNo={}, memberId={}, amount={}, direction={}", entity.getOrderNo(), memberId, amount, direction);
        return toDomain(entity);
    }

    @Override
    public PaymentChannelRoute payOrder(Long orderId, ChannelCode channel) {
        PaymentOrderEntity order = paymentOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在: id=" + orderId));
        if (order.getStatus() != PaymentOrderStatus.PENDING) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "订单状态不允许支付: " + order.getStatus());
        }
        Instant now = Instant.now();

        // 创建渠道路由记录
        PaymentChannelRouteEntity route = new PaymentChannelRouteEntity();
        route.setPaymentOrderId(order.getId());
        route.setChannelCode(channel);
        route.setChannelMerchant("DEMO_MERCHANT_" + channel.name());
        route.setChannelOrderNo(SnowflakeUtils.nextId("CH"));
        route.setStatus(ChannelRouteStatus.PENDING);
        route.setAttemptCount(0);
        route.setCreatedAt(now);
        route.setUpdatedAt(now);
        paymentChannelRouteRepository.save(route);

        // 模拟支付成功：更新路由与订单
        route.setChannelRespCode("SUCCESS");
        route.setChannelRespMsg("支付成功");
        route.setStatus(ChannelRouteStatus.SUCCESS);
        route.setAttemptCount(route.getAttemptCount() + 1);
        route.setUpdatedAt(Instant.now());

        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaidAt(Instant.now());
        paymentOrderRepository.save(order);
        paymentChannelRouteRepository.save(route);

        log.info("Payment order paid: orderNo={}, channel={}, routeId={}", order.getOrderNo(), channel, route.getId());
        return toDomain(route);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrder getPaymentOrder(String orderNo) {
        PaymentOrderEntity entity = paymentOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在: orderNo=" + orderNo));
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrder> listMemberOrders(Long memberId) {
        return paymentOrderRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(this::toDomain)
                .toList();
    }

    // ===== helpers =====

    private PaymentOrder toDomain(PaymentOrderEntity e) {
        return new PaymentOrder(e.getId(), e.getOrderNo(), e.getMemberId(), e.getDirection(),
                e.getAmount(), e.getBusinessType(), e.getRefType(), e.getRefId(),
                e.getStatus(), e.getExpireAt(), e.getCreatedAt(), e.getPaidAt());
    }

    private PaymentChannelRoute toDomain(PaymentChannelRouteEntity e) {
        return new PaymentChannelRoute(e.getId(), e.getPaymentOrderId(), e.getChannelCode(),
                e.getChannelMerchant(), e.getChannelOrderNo(), e.getChannelRespCode(), e.getChannelRespMsg(),
                e.getStatus(), e.getAttemptCount(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
