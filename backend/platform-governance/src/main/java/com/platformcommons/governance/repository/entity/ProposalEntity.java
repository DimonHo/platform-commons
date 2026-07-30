package com.platformcommons.governance.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 提案实体（JPA 持久化）。
 *
 * <p>阿里规范：POJO/Entity 必须重写 {@code toString()}；
 * 包装类型字段使用 {@code equals} 比较；表名使用下划线命名。</p>
 */
@Entity
@Table(name = "proposal")
public class ProposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "proposer_id", nullable = false)
    private Long proposerId;

    @Column(name = "target_chamber", length = 32)
    private String targetChamber;

    @Column(name = "voting_start_at")
    private LocalDateTime votingStartAt;

    @Column(name = "voting_end_at")
    private LocalDateTime votingEndAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProposalEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProposerId() {
        return proposerId;
    }

    public void setProposerId(Long proposerId) {
        this.proposerId = proposerId;
    }

    public String getTargetChamber() {
        return targetChamber;
    }

    public void setTargetChamber(String targetChamber) {
        this.targetChamber = targetChamber;
    }

    public LocalDateTime getVotingStartAt() {
        return votingStartAt;
    }

    public void setVotingStartAt(LocalDateTime votingStartAt) {
        this.votingStartAt = votingStartAt;
    }

    public LocalDateTime getVotingEndAt() {
        return votingEndAt;
    }

    public void setVotingEndAt(LocalDateTime votingEndAt) {
        this.votingEndAt = votingEndAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProposalEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProposalEntity{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", type='" + type + '\''
                + ", status='" + status + '\''
                + ", proposerId=" + proposerId
                + ", targetChamber='" + targetChamber + '\''
                + ", votingStartAt=" + votingStartAt
                + ", votingEndAt=" + votingEndAt
                + ", createdAt=" + createdAt
                + '}';
    }
}
