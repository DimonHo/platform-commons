package com.platformcommons.earlywarning.api;

import com.platformcommons.common.Result;
import com.platformcommons.earlywarning.api.dto.AlertResponse;
import com.platformcommons.earlywarning.domain.AlertCategory;
import com.platformcommons.earlywarning.domain.AlertLevel;
import com.platformcommons.earlywarning.domain.EarlyWarningAlert;
import com.platformcommons.earlywarning.domain.RedLine;
import com.platformcommons.earlywarning.service.EarlyWarningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 防异化预警接口。
 */
@RestController
@RequestMapping("/api/early-warning")
public class EarlyWarningController {

    private static final Logger log = LoggerFactory.getLogger(EarlyWarningController.class);

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
    public Result<List<AlertResponse>> detect(@RequestParam String redLineCode,
                                              @RequestParam String sourceMetric) {
        RedLine redLine = RedLine.valueOf(redLineCode);
        log.info("Detect redLine: code={}, metric={}", redLineCode, sourceMetric);
        List<EarlyWarningAlert> alerts = earlyWarningService.detectRedLine(redLine, sourceMetric);
        return Result.success(alerts.stream().map(EarlyWarningController::toResponse).toList());
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
    public Result<AlertResponse> raise(@RequestParam AlertLevel level,
                                       @RequestParam AlertCategory category,
                                       @RequestParam String title,
                                       @RequestParam(required = false) String description) {
        log.info("Raise alert: level={}, category={}, title={}", level, category, title);
        return Result.success(toResponse(earlyWarningService.raiseAlert(level, category, title, description)));
    }

    /**
     * 解除预警（需监察委员会确认）。
     *
     * @param alertId     预警 ID
     * @param confirmerId 确认人 ID
     * @return 已解除的预警
     */
    @PostMapping("/alerts/{alertId}/clear")
    public Result<AlertResponse> clear(@PathVariable UUID alertId,
                                       @RequestParam String confirmerId) {
        log.info("Clear alert: id={}, confirmer={}", alertId, confirmerId);
        return Result.success(toResponse(earlyWarningService.clearAlert(alertId, confirmerId)));
    }

    /**
     * 查询预警详情。
     *
     * @param alertId 预警 ID
     * @return 预警
     */
    @GetMapping("/alerts/{alertId}")
    public Result<AlertResponse> get(@PathVariable UUID alertId) {
        return earlyWarningService.findById(alertId)
                .map(EarlyWarningController::toResponse)
                .map(Result::success)
                .orElseGet(() -> Result.failure("alert not found: " + alertId));
    }

    /**
     * 查询所有未解除的预警。
     *
     * @return 预警列表
     */
    @GetMapping("/alerts/active")
    public Result<List<AlertResponse>> active() {
        return Result.success(
                earlyWarningService.findActiveAlerts().stream()
                        .map(EarlyWarningController::toResponse)
                        .toList());
    }

    private static AlertResponse toResponse(EarlyWarningAlert a) {
        return new AlertResponse(
                a.id(), a.level(), a.category(),
                a.redLine() == null ? null : a.redLine().code(),
                a.title(), a.description(),
                a.autoMeasureTriggered(), a.acknowledged(),
                a.triggeredAt(), a.clearedAt()
        );
    }
}
