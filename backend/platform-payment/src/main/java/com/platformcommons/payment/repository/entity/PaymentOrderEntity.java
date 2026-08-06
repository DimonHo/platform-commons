package com.platformcommons.payment.repository.entity;

import com.platformcommons.payment.domain.PaymentOrderDirection;
import com.platformcommons.payment.domain.PaymentOrderStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付订单持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "payment_order")
public class PaymentOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "direction", length = 8)
    @Enumerated(EnumType.STRING)
    private PaymentOrderDirection direction;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "business_type", length = 32)
    private String businessType;

    @Column(name = "ref_type", length = 32)
    private String refType;

    @Column(name = "ref_id", length = 64)
    private String refId;

    @Column(name = "status", length = 16)
    @Enumerated(EnumType.STRING)
    private PaymentOrderStatus status;

    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "paid_at")
    private Instant paidAt;
}
