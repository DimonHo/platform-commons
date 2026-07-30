package com.platformcommons.earlywarning.api.dto;

import com.platformcommons.earlywarning.domain.AlertCategory;
import com.platformcommons.earlywarning.domain.AlertLevel;

import java.time.Instant;
import java.util.UUID;

/**
 * 预警响应 DTO。
 *
 * @param id                    预警 ID
 * @param level                 预警等级
 * @param category              预警类别
 * @param redLineCode           红线规则代码（可空）
 * @param title                 标题
 * @param description           描述
 * @param autoMeasureTriggered  是否已自动触发应急措施
 * @param acknowledged          是否经监察委员会确认解除
 * @param triggeredAt           触发时间
 * @param clearedAt             解除时间
 */
public record AlertResponse(
        UUID id,
        AlertLevel level,
        AlertCategory category,
        String redLineCode,
        String title,
        String description,
        boolean autoMeasureTriggered,
        boolean acknowledged,
        Instant triggeredAt,
        Instant clearedAt
) {
}
