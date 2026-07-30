package com.platformcommons.matching.domain;

import java.util.List;

/**
 * 匹配结果。
 *
 * @param taskId     任务 ID
 * @param matchedWorkers 按优先级排序的匹配劳动者列表
 * @param strategyName  使用的匹配策略名称
 * @param explanation 可解释性说明（为何这样排序）
 */
public record MatchResult(
        String taskId,
        List<String> matchedWorkers,
        String strategyName,
        String explanation
) {
}
