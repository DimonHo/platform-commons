package com.platformcommons.ai.repository;

import com.platformcommons.ai.repository.entity.AiReviewRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * AI 审议记录 Repository
 */
public interface AiReviewRecordRepository extends JpaRepository<AiReviewRecordEntity, Long> {

    /**
     * 根据审议编号查询
     *
     * @param reviewId 审议编号
     * @return 审议记录
     */
    Optional<AiReviewRecordEntity> findByReviewId(String reviewId);
}
