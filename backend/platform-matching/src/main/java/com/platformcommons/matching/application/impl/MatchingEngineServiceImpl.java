package com.platformcommons.matching.application.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.matching.domain.match.AntiExploitationConfig;
import com.platformcommons.matching.domain.match.MatchResult;
import com.platformcommons.matching.domain.location.WorkerLocationRepository;
import com.platformcommons.matching.domain.location.WorkerLocationEntity;
import com.platformcommons.matching.application.MatchingEngineService;
import com.platformcommons.matching.domain.match.FairRoundRobinStrategy;
import com.platformcommons.matching.domain.match.MatchingStrategy;
import com.platformcommons.matching.domain.match.NearestFirstStrategy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 匹配引擎服务实现。
 *
 * <p>阿里规范：策略通过工厂方法获取；反榨取约束作为匹配的前置过滤条件。
 * 候选劳动者统一从 {@link WorkerLocationRepository}（JPA）读取，无内存态。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngineServiceImpl implements MatchingEngineService {

    /** 策略名称（UPPER_SNAKE）-> 策略实例。 */
    private static final Map<String, MatchingStrategy> STRATEGIES = Map.of(
            strategyKey(NearestFirstStrategy.class), new NearestFirstStrategy(),
            strategyKey(FairRoundRobinStrategy.class), new FairRoundRobinStrategy()
    );

    private final WorkerLocationRepository workerLocationRepository;

    @Override
    public MatchResult match(String taskId, String strategyName) {
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(strategyName, "strategyName must not be null");

        // 归一化策略名（允许传入 NEAREST_FIRST 或 NearestFirst）
        String normalized = normalizeStrategyName(strategyName);
        MatchingStrategy strategy = STRATEGIES.get(normalized);
        if (strategy == null) {
            throw new BusinessException("unknown strategy: " + strategyName);
        }

        AntiExploitationConfig config = AntiExploitationConfig.DEFAULT;
        // 反榨取预过滤：仅加载活跃订单数未超限的候选，避免全表扫描
        List<MatchingStrategy.WorkerCandidate> candidates =
                workerLocationRepository.findByActiveOrdersLessThan(config.maxActiveOrders()).stream()
                        .map(MatchingEngineServiceImpl::toCandidate)
                        .toList();

        List<String> ranked = strategy.rank(candidates, config);
        String explanation = buildExplanation(strategy.name(), config, ranked.size(), candidates.size());

        log.info("Match executed: taskId={}, strategy={}, matched={}/{}, candidatesFrom=JPA",
                taskId, strategy.name(), ranked.size(), candidates.size());

        return new MatchResult(taskId, ranked, strategy.name(), explanation);
    }

    @Override
    public void registerWorker(String workerId, double latitude, double longitude,
                               int activeOrders, double rating, int registrationDays) {
        Objects.requireNonNull(workerId, "workerId must not be null");

        WorkerLocationEntity entity = new WorkerLocationEntity();
        entity.setWorkerId(workerId);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setActiveOrders(activeOrders);
        entity.setRating(rating);
        entity.setRegistrationDays(registrationDays);
        entity.setUpdatedAt(Instant.now());

        workerLocationRepository.save(entity);

        log.info("Worker registered: workerId={}, lat={}, lng={}, activeOrders={}",
                workerId, latitude, longitude, activeOrders);
    }

    @Override
    public List<String> listWorkers() {
        return workerLocationRepository.findAll().stream()
                .map(WorkerLocationEntity::getWorkerId)
                .toList();
    }

    /**
     * 策略类名转规范键名：去掉 {@code Strategy} 后缀并将 CamelCase 转 UPPER_SNAKE。
     *
     * @param clazz 策略实现类
     * @return 键名，如 {@code NEAREST_FIRST}
     */
    private static String strategyKey(Class<? extends MatchingStrategy> clazz) {
        return clazz.getSimpleName().replace("Strategy", "")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();
    }

    private static String normalizeStrategyName(String name) {
        // 兼容 NEAREST_FIRST / NearestFirst / nearest_first
        String upper = name.toUpperCase().replace("-", "_");
        if (STRATEGIES.containsKey(upper)) {
            return upper;
        }
        // 尝试从类名转换
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase().replace("STRATEGY", "");
    }

    private static MatchingStrategy.WorkerCandidate toCandidate(WorkerLocationEntity e) {
        return new MatchingStrategy.WorkerCandidate(
                e.getWorkerId(), 0.0, e.getActiveOrders(), e.getRating(), e.getRegistrationDays()
        );
    }

    private static String buildExplanation(String strategyName, AntiExploitationConfig config,
                                           int matched, int total) {
        return "策略 " + strategyName + " 在反榨取约束下（最大半径 " + config.maxMatchRadiusKm()
                + "km，最大活跃订单 " + config.maxActiveOrders()
                + "，新人保护 " + config.newcomerProtectionDays() + " 天）"
                + "从 " + total + " 名候选中匹配到 " + matched + " 名劳动者。";
    }
}
