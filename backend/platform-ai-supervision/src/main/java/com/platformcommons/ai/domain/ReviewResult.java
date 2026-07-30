package com.platformcommons.ai.domain;

import java.util.Set;

/**
 * 审议结果（第12章 第64-66条）
 * <p>
 * 记录多角色交叉审议的最终结论，包含：
 * 受益者分析、成本承担者分析、替代方案、分歧记录。
 *
 * @param reviewId            审议编号
 * @param beneficiaries       受益者群体
 * @param costBearers         成本承担者群体
 * @param alternativeProposal 替代方案（如有）
 * @param dissentingViews     分歧意见集合
 * @param consensusReached    是否达成共识
 * @param summary             审议总结
 */
public record ReviewResult(
        String reviewId,
        Set<String> beneficiaries,
        Set<String> costBearers,
        String alternativeProposal,
        Set<String> dissentingViews,
        boolean consensusReached,
        String summary
) {
}
