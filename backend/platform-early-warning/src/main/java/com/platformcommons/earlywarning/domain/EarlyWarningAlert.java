package com.platformcommons.earlywarning.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 预警实体。
 *
 * @param id              预警 ID
 * @param level           预警等级
 * @param category        预警类别
 * @param redLine         触发的红线规则（可空，非红线预警时为 null）
 * @param title           预警标题
 * @param description     预警描述
 * @param sourceMetric    触发指标值
 * @param threshold       阈值
 * @param autoMeasureTriggered 是否已自动触发应急措施
 * @param acknowledged    是否经监察委员会确认解除
 * @param acknowledgedBy  解除确认人
 * @param triggeredAt     触发时间
 * @param clearedAt       解除时间
 */
public record EarlyWarningAlert(
        UUID id,
        AlertLevel level,
        AlertCategory category,
        RedLine redLine,
        String title,
        String description,
        String sourceMetric,
        String threshold,
        boolean autoMeasureTriggered,
        boolean acknowledged,
        String acknowledgedBy,
        Instant triggeredAt,
        Instant clearedAt
) {
}
