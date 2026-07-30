package com.platformcommons.techgovernance.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 算法规格说明（第13章 第74-76条）
 * <p>
 * 核心算法必须公开以下规格说明，供公众审计：
 * 目标函数、输入定义、权重范围、约束条件、版本号。
 *
 * @param algorithmName  算法名称
 * @param version        算法版本
 * @param objective      目标函数描述
 * @param inputs         输入定义（字段名 → 说明）
 * @param weightRanges   权重范围（参数名 → [最小值, 最大值]）
 * @param constraints    约束条件列表
 * @param isCritical     是否为关键权限算法
 */
public record AlgorithmSpec(
        String algorithmName,
        String version,
        String objective,
        Map<String, String> inputs,
        Map<String, BigDecimal[]> weightRanges,
        List<String> constraints,
        boolean isCritical
) {
}
