package com.platformcommons.earlywarning.api;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.earlywarning.api.dto.AlertResponse;
import com.platformcommons.earlywarning.domain.AlertCategory;
import com.platformcommons.earlywarning.domain.AlertLevel;
import com.platformcommons.earlywarning.domain.EarlyWarningAlert;
import com.platformcommons.earlywarning.domain.RedLine;
import com.platformcommons.earlywarning.service.EarlyWarningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 防异化预警接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@RestController
@RequestMapping("/api/early-warning")
@Slf4j
public class EarlyWarningController {


    private final EarlyWarningService earlyWarningService;

    public EarlyWarningController(EarlyWarningService earlyWarningService) {
        this.earlyWarningService = earlyWarningService;
    }

    /**
     * 检测红线。
     *
     * @param redLineCode 红线规则代码（如 R-CAP-01）
     * @param sourceMetric 当前指标值
     * @return 触发的预警列表（可能为空）
     */
    @PostMapping("/detect")
    public List<AlertResponse> detect(@RequestParam String redLineCode,
                                      @RequestParam String sourceMetric) {
        RedLine redLine = RedLine.valueOf(redLineCode);
        log.info("Detect redLine: code={}, metric={}", redLineCode, sourceMetric);
        List<EarlyWarningAlert> alerts = earlyWarningService.detectRedLine(redLine, sourceMetric);
        return alerts.stream().map(EarlyWarningController::toResponse).toList();
    }

    /**
     * 手动创建预警。
     *
     * @param level       预警等级
     * @param category    预警类别
     * @param title       标题
     * @param description 描述
     * @return 创建的预警
     */
    @PostMapping("/alerts")
    public AlertResponse raise(@RequestParam AlertLevel level,
                               @RequestParam AlertCategory category,
                               @RequestParam String title,
                               @RequestParam(required = false) String description) {
        log.info("Raise alert: level={}, category={}, title={}", level, category, title);
        return toResponse(earlyWarningService.raiseAlert(level, category, title, description));
    }

    /**
     * 解除预警（需监察委员会确认）。
     *
     * @param alertId     预警 ID
     * @param confirmerId 确认人 ID
     * @return 已解除的预警
     */
    @PostMapping("/alerts/{alertId}/clear")
    public AlertResponse clear(@PathVariable UUID alertId,
                               @RequestParam String confirmerId) {
        log.info("Clear alert: id={}, confirmer={}", alertId, confirmerId);
        return toResponse(earlyWarningService.clearAlert(alertId, confirmerId));
    }

    /**
     * 查询预警详情。
     *
     * @param alertId 预警 ID
     * @return 预警
     */
    @GetMapping("/alerts/{alertId}")
    public AlertResponse get(@PathVariable UUID alertId) {
        return earlyWarningService.findById(alertId)
                .map(EarlyWarningController::toResponse)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND,
                        "预警不存在: " + alertId));
    }

    /**
     * 查询所有未解除的预警。
     *
     * @return 预警列表
     */
    @GetMapping("/alerts/active")
    public List<AlertResponse> active() {
        return earlyWarningService.findActiveAlerts().stream()
                .map(EarlyWarningController::toResponse)
                .toList();
    }

    private static AlertResponse toResponse(EarlyWarningAlert a) {
        return RecordUtils.copy(a, AlertResponse.class, Map.of(
                "redLineCode", a.redLine() == null ? null : a.redLine().code()
        ));
    }
}
