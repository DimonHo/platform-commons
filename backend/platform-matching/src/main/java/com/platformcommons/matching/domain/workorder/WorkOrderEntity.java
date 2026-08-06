package com.platformcommons.matching.domain.workorder;

import com.platformcommons.matching.domain.workorder.OrderPriority;
import com.platformcommons.matching.domain.workorder.WorkOrderStatus;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 业务工单持久化实体，映射 {@code work_order} 表。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "work_order")
@EqualsAndHashCode(of = "id")
public class WorkOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 32)
    private WorkOrderType orderType;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "chamber", length = 32)
    private String chamber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private WorkOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 16)
    private OrderPriority priority;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason", length = 256)
    private String cancelReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** 乐观锁版本号（V3 迁移已建列，由 JPA 托管，并发抢单时防止丢失更新）。 */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
