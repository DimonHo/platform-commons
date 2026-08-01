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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * AI 审议记录实体
 */
@Entity
@Table(name = "ai_review_records")
@Getter
@Setter
@ToString
@NoArgsConstructor
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
