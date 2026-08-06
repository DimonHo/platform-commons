package com.platformcommons.matching.domain.dispatch;

import com.platformcommons.matching.domain.dispatch.GrabStatus;
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
 * 抢单记录持久化实体，映射 {@code dispatch_grab_record} 表。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "dispatch_grab_record")
@EqualsAndHashCode(of = "id")
public class DispatchGrabRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "broadcast_id", nullable = false)
    private Long broadcastId;

    @Column(name = "worker_id", nullable = false)
    private Long workerId;

    @Column(name = "worker_lat")
    private Double workerLat;

    @Column(name = "worker_lng")
    private Double workerLng;

    @Column(name = "distance_meters")
    private Integer distanceMeters;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private GrabStatus status;

    @Column(name = "grabbed_at", nullable = false)
    private Instant grabbedAt;
}
