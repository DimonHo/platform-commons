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

import java.util.Objects;

/**
 * 争议记录实体
 */
@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisputeId() {
        return disputeId;
    }

    public void setDisputeId(String disputeId) {
        this.disputeId = disputeId;
    }

    public String getFiledBy() {
        return filedBy;
    }

    public void setFiledBy(String filedBy) {
        this.filedBy = filedBy;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DisputeLevel getLevel() {
        return level;
    }

    public void setLevel(DisputeLevel level) {
        this.level = level;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFiledAt() {
        return filedAt;
    }

    public void setFiledAt(String filedAt) {
        this.filedAt = filedAt;
    }

    @Override
    public String toString() {
        return "DisputeEntity{"
                + "id=" + id
                + ", disputeId='" + disputeId + '\''
                + ", filedBy='" + filedBy + '\''
                + ", subject='" + subject + '\''
                + ", level=" + level
                + ", status=" + status
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DisputeEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
