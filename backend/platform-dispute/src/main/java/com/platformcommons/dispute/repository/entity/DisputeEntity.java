package com.platformcommons.dispute.repository.entity;

import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.domain.DisputeStatus;
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
 * 争议记录实体
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "dispute_records")
public class DisputeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String disputeId;

    @Column(nullable = false, length = 64)
    private String filedBy;

    @Column(nullable = false, length = 256)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DisputeLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DisputeStatus status;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(length = 32)
    private String filedAt;
}
