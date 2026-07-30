package com.platformcommons.matching.api.dto;

import java.util.List;

/**
 * 匹配响应 DTO。
 *
 * @param taskId        任务 ID
 * @param matchedWorkers 匹配到的劳动者列表
 * @param strategyName  使用的策略名称
 * @param explanation   可解释性说明
 */
public record MatchResponse(
        String taskId,
        List<String> matchedWorkers,
        String strategyName,
        String explanation
) {
}
