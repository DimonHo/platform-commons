package com.platformcommons.identity.repository.entity;

import com.platformcommons.identity.domain.VehicleType;
import com.platformcommons.identity.domain.WorkerOnlineStatus;
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

/**
 * 劳动者档案实体（worker_profile 表）。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "worker_profile")
@EqualsAndHashCode(of = "id")
public class WorkerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "service_categories", length = 256)
    private String serviceCategories;

    @Column(name = "service_radius_m")
    private Integer serviceRadiusM;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", length = 32)
    private VehicleType vehicleType;

    @Column(name = "vehicle_plate", length = 32)
    private String vehiclePlate;

    @Column(length = 256)
    private String skills;

    @Column(name = "max_concurrent")
    private Integer maxConcurrent;

    @Column
    private Double rating;

    @Column(name = "total_completed")
    private Integer totalCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_status", length = 16)
    private WorkerOnlineStatus onlineStatus;

    @Column(length = 512)
    private String bio;
}
