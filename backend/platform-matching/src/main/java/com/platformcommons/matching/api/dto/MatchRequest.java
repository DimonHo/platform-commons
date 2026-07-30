package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 匹配请求 DTO。
 *
 * @param taskId       任务 ID
 * @param strategyName 匹配策略名称
 */
public record MatchRequest(
        @NotBlank(message = "taskId must not be blank")
        String taskId,

        @NotBlank(message = "strategyName must not be blank")
        String strategyName
) {
}
