package com.platformcommons.matching.strategy;

import com.platformcommons.matching.domain.AntiExploitationConfig;

import java.util.Comparator;
import java.util.List;

/**
 * 公平轮转策略：优先匹配活跃订单最少的劳动者，保证订单分配公平，
 * 同时尊重反榨取约束（半径与超载过滤、新人保护）。
 */
public final class FairRoundRobinStrategy implements MatchingStrategy {

    @Override
    public List<String> rank(List<WorkerCandidate> candidates, AntiExploitationConfig config) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(c -> c.distanceKm() <= config.maxMatchRadiusKm())
                .filter(c -> c.activeOrders() < config.maxActiveOrders())
                .filter(c -> c.registrationDays() >= config.newcomerProtectionDays())
                .sorted(Comparator.comparingInt(WorkerCandidate::activeOrders)
                        .thenComparingDouble(WorkerCandidate::distanceKm))
                .map(WorkerCandidate::workerId)
                .toList();
    }

    @Override
    public String name() {
        return "FAIR_ROUND_ROBIN";
    }
}
