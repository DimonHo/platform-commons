package com.platformcommons.governance.api.dto;

/**
 * 投票结果统计响应 DTO。
 *
 * @param proposalId    提案 ID
 * @param title         提案标题
 * @param status        提案当前状态
 * @param yesCount      赞成票数
 * @param noCount       反对票数
 * @param abstainCount  弃权票数
 * @param totalCount    总票数
 */
public record VoteResultResponse(
        Long proposalId,
        String title,
        String status,
        long yesCount,
        long noCount,
        long abstainCount,
        long totalCount
) {
}
