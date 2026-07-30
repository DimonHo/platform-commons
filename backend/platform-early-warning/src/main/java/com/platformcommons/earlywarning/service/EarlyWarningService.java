package com.platformcommons.earlywarning.service;

import com.platformcommons.earlywarning.domain.AlertLevel;
import com.platformcommons.earlywarning.domain.EarlyWarningAlert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 防异化预警服务接口。
 */
public interface EarlyWarningService {

    /**
     * 检测红线：根据输入指标判断是否触发某条红线，若触发则创建 RED 级预警并自动启动应急措施。
     *
     * @param redLine      红线规则
     * @param sourceMetric 当前指标值
     * @return 触发的预警（若未触发返回空列表）
     */
    List<EarlyWarningAlert> detectRedLine(com.platformcommons.earlywarning.domain.RedLine redLine, String sourceMetric);

    /**
     * 创建预警。
     *
     * @param level       预警等级
     * @param category    预警类别
     * @param title       标题
     * @param description 描述
     * @return 创建的预警
     */
    EarlyWarningAlert raiseAlert(AlertLevel level,
                                 com.platformcommons.earlywarning.domain.AlertCategory category,
                                 String title, String description);

    /**
     * 解除预警：必须经监察委员会确认。
     *
     * @param alertId         预警 ID
     * @param confirmerId     监察委员会成员 ID
     * @return 已解除的预警
     */
    EarlyWarningAlert clearAlert(UUID alertId, String confirmerId);

    /**
     * 查询预警。
     *
     * @param alertId 预警 ID
     * @return 预警（可能不存在）
     */
    Optional<EarlyWarningAlert> findById(UUID alertId);

    /**
     * 查询所有未解除的预警。
     *
     * @return 预警列表
     */
    List<EarlyWarningAlert> findActiveAlerts();
}
