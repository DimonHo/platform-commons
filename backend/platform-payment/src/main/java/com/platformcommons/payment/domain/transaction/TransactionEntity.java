package com.platformcommons.payment.domain.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 交易持久化实体（映射 V3 迁移的 {@code payment_transaction} 表）。
 *
 * <p>主键由 Java 侧 {@link UUID#randomUUID()} 生成；{@code order_id} 唯一约束作为幂等兜底。</p>
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "payment_transaction")
public class TransactionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** 业务订单号（幂等键）。 */
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    /** 劳动者 ID。 */
    @Column(name = "worker_id", length = 64)
    private String workerId;

    /** 发包方 ID。 */
    @Column(name = "requester_id", length = 64)
    private String requesterId;

    /** 订单总价。 */
    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmount;

    /** 平台服务费。 */
    @Column(name = "platform_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal platformFee;

    /** 劳动者返还。 */
    @Column(name = "worker_share", nullable = false, precision = 19, scale = 4)
    private BigDecimal workerShare;

    /** 交易状态。 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TransactionStatus status;

    /** 记账时使用的分账规则版本。 */
    @Column(name = "rule_version", length = 32)
    private String ruleVersion;

    /** 创建时间。 */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** 结算完成时间。 */
    @Column(name = "settled_at")
    private Instant settledAt;
}
