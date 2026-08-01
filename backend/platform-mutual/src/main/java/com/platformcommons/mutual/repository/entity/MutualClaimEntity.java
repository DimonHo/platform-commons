package com.platformcommons.mutual.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 互助理赔申请持久化实体。
 *
 * <p>阿里规范：实体必须重写 equals/hashCode/toString，基于业务主键 id。
 */
@Entity
@Table(name = "mutual_claim")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MutualClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "applicant_id", nullable = false, length = 64)
    private String applicantId;

    @Column(name = "incident_type", nullable = false, length = 32)
    private String incidentType;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "claimed_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal claimedAmount;

    @Column(name = "evidence_urls", length = 2048)
    private String evidenceUrls;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewer_id", length = 64)
    private String reviewerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MutualClaimEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
