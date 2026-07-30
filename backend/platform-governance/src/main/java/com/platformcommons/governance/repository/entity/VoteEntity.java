package com.platformcommons.governance.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 投票记录实体（JPA 持久化）。
 *
 * <p>阿里规范：POJO/Entity 必须重写 {@code toString()}；
 * 包装类型字段使用 {@code equals} 比较；表名使用下划线命名。
 * 唯一约束保证一名成员对同一提案仅可投票一次。</p>
 */
@Entity
@Table(name = "vote", uniqueConstraints = {
        @UniqueConstraint(name = "uk_proposal_voter", columnNames = {"proposal_id", "voter_id"})
})
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(nullable = false, length = 16)
    private String choice;

    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    public VoteEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public Long getVoterId() {
        return voterId;
    }

    public void setVoterId(Long voterId) {
        this.voterId = voterId;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VoteEntity that)) {
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
        return "VoteEntity{"
                + "id=" + id
                + ", proposalId=" + proposalId
                + ", voterId=" + voterId
                + ", choice='" + choice + '\''
                + ", votedAt=" + votedAt
                + '}';
    }
}
