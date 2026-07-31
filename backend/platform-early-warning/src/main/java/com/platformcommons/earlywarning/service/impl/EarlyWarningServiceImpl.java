package com.platformcommons.earlywarning.service.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.earlywarning.domain.AlertCategory;
import com.platformcommons.earlywarning.domain.AlertLevel;
import com.platformcommons.earlywarning.domain.EarlyWarningAlert;
import com.platformcommons.earlywarning.domain.RedLine;
import com.platformcommons.earlywarning.repository.AlertRepository;
import com.platformcommons.earlywarning.repository.entity.AlertEntity;
import com.platformcommons.earlywarning.service.EarlyWarningService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 防异化预警服务实现。
 *
 * <p>映射宪章第16章 100-101条：红线触发后自动启动应急措施；解除需监察委员会确认。
 */
@Service
@Slf4j
public class EarlyWarningServiceImpl implements EarlyWarningService {


    /** 各红线的阈值定义。 */
    private static final Map<RedLine, String> THRESHOLDS = Map.of(
            RedLine.SINGLE_CAPITAL_CONCENTRATION, "20%",
            RedLine.WORKER_INCOME_BELOW_FLOOR, "70%",
            RedLine.ALGORITHM_NOT_AUDITABLE, "open+reproducible",
            RedLine.GOVERNANCE_CAPTURED, "independent",
            RedLine.EMERGENCY_OVER_LIMIT, "14d"
    );

    private final AlertRepository alertRepository;

    /** 预警内存存储（演示用）。 */
    private final Map<UUID, EarlyWarningAlert> alertStore = new ConcurrentHashMap<>();

    public EarlyWarningServiceImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Override
    public List<EarlyWarningAlert> detectRedLine(RedLine redLine, String sourceMetric) {
        Objects.requireNonNull(redLine, "redLine must not be null");
        Objects.requireNonNull(sourceMetric, "sourceMetric must not be null");

        if (!isBreached(redLine, sourceMetric)) {
            log.info("RedLine {} not breached: metric={}", redLine.code(), sourceMetric);
            return List.of();
        }

        List<EarlyWarningAlert> triggered = new ArrayList<>();
        Instant now = Instant.now();
        EarlyWarningAlert alert = new EarlyWarningAlert(
                UUID.randomUUID(),
                AlertLevel.RED,
                redLine.category(),
                redLine,
                "红线触发：" + redLine.description(),
                "指标 " + sourceMetric + " 超过阈值 " + THRESHOLDS.get(redLine),
                sourceMetric,
                THRESHOLDS.get(redLine),
                true,
                false,
                null,
                now,
                null
        );
        alertStore.put(alert.id(), alert);
        persist(alert);
        triggered.add(alert);

        log.warn("RedLine breached: code={}, metric={}, autoMeasureTriggered=true",
                redLine.code(), sourceMetric);
        return triggered;
    }

    @Override
    public EarlyWarningAlert raiseAlert(AlertLevel level, AlertCategory category,
                                        String title, String description) {
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(title, "title must not be null");

        Instant now = Instant.now();
        // 仅 RED 级触发自动措施
        boolean autoMeasure = AlertLevel.RED.equals(level);

        EarlyWarningAlert alert = new EarlyWarningAlert(
                UUID.randomUUID(), level, category, null,
                title, description, null, null,
                autoMeasure, false, null, now, null
        );
        alertStore.put(alert.id(), alert);
        persist(alert);

        log.info("Alert raised: id={}, level={}, category={}, title={}", alert.id(), level, category, title);
        return alert;
    }

    @Override
    public EarlyWarningAlert clearAlert(UUID alertId, String confirmerId) {
        Objects.requireNonNull(alertId, "alertId must not be null");
        Objects.requireNonNull(confirmerId, "confirmerId must not be null");

        EarlyWarningAlert alert = alertStore.get(alertId);
        if (alert == null) {
            throw new BusinessException("alert not found: " + alertId);
        }
        if (alert.acknowledged()) {
            throw new BusinessException("alert already cleared: " + alertId);
        }

        Instant now = Instant.now();
        EarlyWarningAlert cleared = new EarlyWarningAlert(
                alert.id(), alert.level(), alert.category(), alert.redLine(),
                alert.title(), alert.description(), alert.sourceMetric(), alert.threshold(),
                alert.autoMeasureTriggered(), true, confirmerId, alert.triggeredAt(), now
        );
        alertStore.put(alertId, cleared);
        persist(cleared);

        log.info("Alert cleared: id={}, confirmer={}", alertId, confirmerId);
        return cleared;
    }

    @Override
    public Optional<EarlyWarningAlert> findById(UUID alertId) {
        return Optional.ofNullable(alertStore.get(alertId));
    }

    @Override
    public List<EarlyWarningAlert> findActiveAlerts() {
        return alertStore.values().stream()
                .filter(a -> !a.acknowledged())
                .sorted((a1, a2) -> a2.triggeredAt().compareTo(a1.triggeredAt()))
                .toList();
    }

    /**
     * 判断指标是否突破红线阈值。
     *
     * <p>简化实现：以数值比较判定。生产环境应针对每条红线设置专用判定逻辑。
     */
    private boolean isBreached(RedLine redLine, String sourceMetric) {
        try {
            double metric = Double.parseDouble(sourceMetric.replaceAll("[^0-9.]", ""));
            return switch (redLine) {
                case SINGLE_CAPITAL_CONCENTRATION -> metric > 20.0;
                case WORKER_INCOME_BELOW_FLOOR -> metric < 70.0;
                case EMERGENCY_OVER_LIMIT -> metric > 14.0;
                // 以下两条需定性判定，此处以非空视为"已开源/独立"未触发
                case ALGORITHM_NOT_AUDITABLE, GOVERNANCE_CAPTURED -> false;
            };
        } catch (NumberFormatException e) {
            log.warn("Cannot parse sourceMetric for redLine {}: '{}'", redLine.code(), sourceMetric);
            return false;
        }
    }

    private void persist(EarlyWarningAlert a) {
        AlertEntity e = new AlertEntity();
        e.setId(a.id());
        e.setLevel(a.level());
        e.setCategory(a.category());
        e.setRedLineCode(a.redLine() == null ? null : a.redLine().code());
        e.setTitle(a.title());
        e.setDescription(a.description());
        e.setSourceMetric(a.sourceMetric());
        e.setThreshold(a.threshold());
        e.setAutoMeasureTriggered(a.autoMeasureTriggered());
        e.setAcknowledged(a.acknowledged());
        e.setAcknowledgedBy(a.acknowledgedBy());
        e.setTriggeredAt(a.triggeredAt());
        e.setClearedAt(a.clearedAt());
        alertRepository.save(e);
    }
}
