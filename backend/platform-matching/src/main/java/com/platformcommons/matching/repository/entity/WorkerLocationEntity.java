package com.platformcommons.matching.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

/**
 * 劳动者位置持久化实体。
 */
@Entity
@Table(name = "worker_location")
@Getter
@Setter
@ToString
@NoArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkerLocationEntity that)) {
            return false;
        }
        return Objects.equals(workerId, that.workerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId);
    }
}
