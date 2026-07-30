package com.platformcommons.ai.api.dto;

import java.io.Serializable;
import java.util.Set;

/**
 * 审议响应 DTO
 *
 * @param reviewId            审议编号
 * @param beneficiaries       受益者群体
 * @param costBearers         成本承担者群体
 * @param alternativeProposal 替代方案
 * @param dissentingViews     分歧意见
 * @param consensusReached    是否达成共识
 * @param summary             审议总结
 */
public record ReviewResponse(
        String reviewId,
        Set<String> beneficiaries,
        Set<String> costBearers,
        String alternativeProposal,
        Set<String> dissentingViews,
        boolean consensusReached,
        String summary
) implements Serializable {
}
