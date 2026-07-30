package com.platformcommons.ai.repository.entity;

import com.platformcommons.ai.domain.MandatoryReviewItem;
import com.platformcommons.ai.domain.ReviewStatus;
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
 * AI 审议记录实体
 */
@Entity
@Table(name = "ai_review_records")
public class AiReviewRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String reviewId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private MandatoryReviewItem mandatoryItem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String proposal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReviewStatus status;

    @Column
    private Boolean consensusReached;

    @Column(columnDefinition = "TEXT")
    private String dissent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public MandatoryReviewItem getMandatoryItem() {
        return mandatoryItem;
    }

    public void setMandatoryItem(MandatoryReviewItem mandatoryItem) {
        this.mandatoryItem = mandatoryItem;
    }

    public String getProposal() {
        return proposal;
    }

    public void setProposal(String proposal) {
        this.proposal = proposal;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Boolean getConsensusReached() {
        return consensusReached;
    }

    public void setConsensusReached(Boolean consensusReached) {
        this.consensusReached = consensusReached;
    }

    public String getDissent() {
        return dissent;
    }

    public void setDissent(String dissent) {
        this.dissent = dissent;
    }

    @Override
    public String toString() {
        return "AiReviewRecordEntity{"
                + "id=" + id
                + ", reviewId='" + reviewId + '\''
                + ", mandatoryItem=" + mandatoryItem
                + ", status=" + status
                + ", consensusReached=" + consensusReached
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AiReviewRecordEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
