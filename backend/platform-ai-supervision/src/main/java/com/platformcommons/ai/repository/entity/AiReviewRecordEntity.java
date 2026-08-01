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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 审议记录实体
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
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
}
