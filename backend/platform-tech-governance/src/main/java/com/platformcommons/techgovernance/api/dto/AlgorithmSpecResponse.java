package com.platformcommons.techgovernance.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 算法规格响应 DTO
 *
 * @param algorithmId    算法ID
 * @param algorithmName  算法名称
 * @param version        版本
 * @param objective      目标函数
 * @param inputs         输入定义
 * @param weightRanges   权重范围
 * @param constraints    约束条件
 * @param critical       是否关键算法
 */
public record AlgorithmSpecResponse(
        String algorithmId,
        String algorithmName,
        String version,
        String objective,
        Map<String, String> inputs,
        Map<String, BigDecimal[]> weightRanges,
        List<String> constraints,
        boolean critical
) implements Serializable {
}
