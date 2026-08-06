package com.platformcommons.payment.domain.gateway;

import com.platformcommons.payment.domain.gateway.ChannelCode;
import com.platformcommons.payment.domain.gateway.ChannelRouteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 支付渠道路由持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "payment_channel_route")
public class PaymentChannelRouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "payment_order_id", nullable = false)
    private Long paymentOrderId;

    @Column(name = "channel_code", length = 32)
    @Enumerated(EnumType.STRING)
    private ChannelCode channelCode;

    @Column(name = "channel_merchant", length = 64)
    private String channelMerchant;

    @Column(name = "channel_order_no", length = 64)
    private String channelOrderNo;

    @Column(name = "channel_resp_code", length = 16)
    private String channelRespCode;

    @Column(name = "channel_resp_msg", length = 256)
    private String channelRespMsg;

    @Column(name = "status", length = 16)
    @Enumerated(EnumType.STRING)
    private ChannelRouteStatus status;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
