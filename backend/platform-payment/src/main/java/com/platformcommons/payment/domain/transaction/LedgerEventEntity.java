package com.platformcommons.payment.domain.transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 账本事件持久化实体。
 *
 * <p>阿里规范：POJO 类必须重写 equals/hashCode/toString；数据库表必须有主键。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "payment_ledger_event")
public class LedgerEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** V2 新增：会员 ID（可空）。 */
    @Column(name = "member_id")
    private Long memberId;

    /** V2 新增：支付渠道编码（可空）。 */
    @Column(name = "channel_code", length = 32)
    private String channelCode;

    /** V2 新增：订单号（可空）。 */
    @Column(name = "order_no", length = 64)
    private String orderNo;

    public LedgerEventEntity(UUID eventId, UUID transactionId, String eventType,
                             BigDecimal amount, Instant occurredAt) {
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.eventType = eventType;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }
}
