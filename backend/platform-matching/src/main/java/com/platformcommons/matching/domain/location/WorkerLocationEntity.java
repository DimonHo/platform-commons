package com.platformcommons.matching.domain.location;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 劳动者位置持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "worker_location")
@EqualsAndHashCode(of = "workerId")
public class WorkerLocationEntity {

    @Id
    @Column(name = "worker_id", nullable = false, length = 64)
    private String workerId;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "active_orders", nullable = false)
    private int activeOrders;

    @Column(name = "rating")
    private double rating;

    @Column(name = "registration_days", nullable = false)
    private int registrationDays;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
