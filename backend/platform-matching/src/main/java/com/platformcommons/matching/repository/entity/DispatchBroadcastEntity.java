package com.platformcommons.matching.repository.entity;

import com.platformcommons.matching.domain.BroadcastStatus;
import com.platformcommons.matching.domain.BroadcastType;
import com.platformcommons.matching.domain.WorkOrderType;
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
 * 派单广播持久化实体，映射 {@code dispatch_broadcast} 表。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "dispatch_broadcast")
@EqualsAndHashCode(of = "id")
public class DispatchBroadcastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "broadcast_no", nullable = false, unique = true, length = 64)
    private String broadcastNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 32)
    private WorkOrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "broadcast_type", nullable = false, length = 16)
    private BroadcastType broadcastType;

    @Column(name = "center_lat")
    private Double centerLat;

    @Column(name = "center_lng")
    private Double centerLng;

    @Column(name = "radius_meters")
    private Integer radiusMeters;

    @Column(name = "target_count")
    private Integer targetCount;

    @Column(name = "grabbed_count")
    private Integer grabbedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private BroadcastStatus status;

    @Column(name = "expire_at")
    private Instant expireAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
