package com.platformcommons.ai.service;

import com.platformcommons.ai.domain.MandatoryReviewItem;
import com.platformcommons.ai.domain.ReviewResult;
import com.platformcommons.ai.domain.ReviewStatus;

import java.util.List;
import java.util.Optional;

/**
 * AI 公共监督审议服务（第12章 第60-69条）
 * <p>
 * 对平台核心算法决策进行多角色交叉审议，
 * 确保受益者与成本承担者透明可审计。
 */
public interface AiSupervisionService {

    /**
     * 发起强制审议
     *
     * @param item     强制审议事项
     * @param proposal 待审议的提案内容
     * @return 审议编号
     */
    String initiateReview(MandatoryReviewItem item, String proposal);

    /**
     * 执行多角色交叉审议
     *
     * @param reviewId 审议编号
     * @return 审议结果
     */
    ReviewResult conductReview(String reviewId);

    /**
     * 查询审议状态
     *
     * @param reviewId 审议编号
     * @return 审议状态
     */
    Optional<ReviewStatus> getReviewStatus(String reviewId);

    /**
     * 获取所有审议记录
     *
     * @return 审议结果列表
     */
    List<ReviewResult> listAllReviews();

    /**
     * 提交争议（当审议结果存在分歧时）
     *
     * @param reviewId 审议编号
     * @param dissent  分歧说明
     */
    void contestReview(String reviewId, String dissent);
}
