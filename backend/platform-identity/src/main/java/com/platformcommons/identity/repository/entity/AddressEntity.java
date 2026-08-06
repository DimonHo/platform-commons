package com.platformcommons.identity.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 收货地址实体（address 表）。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "address")
@EqualsAndHashCode(of = "id")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(length = 32)
    private String label;

    @Column(name = "receiver_name", length = 64)
    private String receiverName;

    @Column(length = 20)
    private String phone;

    @Column(length = 32)
    private String province;

    @Column(length = 32)
    private String city;

    @Column(length = 32)
    private String district;

    @Column(length = 256)
    private String detail;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
