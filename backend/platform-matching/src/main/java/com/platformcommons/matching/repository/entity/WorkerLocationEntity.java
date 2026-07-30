package com.platformcommons.matching.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;
import java.util.Objects;

/**
 * 劳动者位置持久化实体。
 */
@Entity
@Table(name = "worker_location")
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

    public WorkerLocationEntity() {
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(int activeOrders) {
        this.activeOrders = activeOrders;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRegistrationDays() {
        return registrationDays;
    }

    public void setRegistrationDays(int registrationDays) {
        this.registrationDays = registrationDays;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

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

    @Override
    public String toString() {
        return "WorkerLocationEntity{workerId='" + workerId
                + "', lat=" + latitude
                + ", lng=" + longitude
                + ", activeOrders=" + activeOrders
                + ", rating=" + rating
                + ", registrationDays=" + registrationDays
                + ", updatedAt=" + updatedAt + '}';
    }
}
