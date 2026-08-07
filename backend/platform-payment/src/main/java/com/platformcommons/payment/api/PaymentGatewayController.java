package com.platformcommons.payment.api;

import com.platformcommons.payment.api.dto.PayRequest;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.payment.api.dto.PaymentOrderResponse;
import com.platformcommons.payment.domain.gateway.PaymentChannelRoute;
import com.platformcommons.payment.domain.paymentorder.PaymentOrder;
import com.platformcommons.payment.application.PaymentGatewayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统一支付网关 REST 接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment-gateway")
@Tag(name = "统一支付网关")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/orders")
    public PaymentOrderResponse createOrder(@Valid @RequestBody PayRequest request) {
        PaymentOrder order = paymentGatewayService.createPaymentOrder(
                request.memberId(), request.direction(), request.amount(),
                request.businessType(), request.refType(), request.refId()
        );
        // 立即使用指定渠道支付
        PaymentChannelRoute route = paymentGatewayService.payOrder(order.id(), request.channel());
        log.info("Order created and paid: orderNo={}, routeStatus={}", order.orderNo(), route.status());
        return toResponse(order);
    }

    @PostMapping("/orders/{orderId}/pay")
    public PaymentOrderResponse payOrder(@PathVariable Long orderId, @RequestBody PayRequest request) {
        paymentGatewayService.payOrder(orderId, request.channel());
        // payOrder 已更新订单状态，通过 orderId 对应的 orderNo 查询最新状态
        // 简化：返回成员订单中第一条匹配 orderId 的订单
        // 由于 PaymentGatewayService.payOrder 内部已更新订单状态，
        // 这里直接按订单号查询最新状态
        return paymentGatewayService.listMemberOrders(request.memberId()).stream()
                .filter(o -> o.id().equals(orderId))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("订单支付后未找到: orderId=" + orderId));
    }

    @GetMapping("/orders/{orderNo}")
    public PaymentOrderResponse getOrder(@PathVariable String orderNo) {
        PaymentOrder order = paymentGatewayService.getPaymentOrder(orderNo);
        return toResponse(order);
    }

    @GetMapping("/members/{memberId}/orders")
    public List<PaymentOrderResponse> listMemberOrders(@PathVariable Long memberId) {
        return paymentGatewayService.listMemberOrders(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentOrderResponse toResponse(PaymentOrder order) {
        return RecordUtils.copy(order, PaymentOrderResponse.class);
    }
}
