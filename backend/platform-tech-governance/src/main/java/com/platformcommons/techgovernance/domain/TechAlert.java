package com.platformcommons.techgovernance.domain;

import java.time.Instant;

/**
 * 技术红线触发（第13章 第77-80条）
 * <p>
 * 当检测到以下情况时触发技术红线告警：
 * - 生产代码与代码库不一致
 * - 算法实际行为与规格说明不符
 * - 关键权限未经授权使用
 * - 构建不可复现
 *
 * @param alertId    告警编号
 * @param alertType  告警类型
 * @param description 告警描述
 * @param triggeredAt 触发时间
 * @param severity   严重级别
 */
public record TechAlert(
        String alertId,
        String alertType,
        String description,
        Instant triggeredAt,
        Severity severity
) {

    /** 严重级别 */
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
