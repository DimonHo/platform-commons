package com.platformcommons.matching.domain.match;

import com.platformcommons.matching.domain.match.AntiExploitationConfig;

import java.util.Comparator;
import java.util.List;

/**
 * 就近优先策略：按距离升序匹配，过滤超出最大半径与超载的劳动者。
 */
public final class NearestFirstStrategy implements MatchingStrategy {

    @Override
    public List<String> rank(List<WorkerCandidate> candidates, AntiExploitationConfig config) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(c -> c.distanceKm() <= config.maxMatchRadiusKm())
                .filter(c -> c.activeOrders() < config.maxActiveOrders())
                .sorted(Comparator.comparingDouble(WorkerCandidate::distanceKm)
                        .thenComparing(WorkerCandidate::activeOrders)
                        .reversed().reversed())
                .map(WorkerCandidate::workerId)
                .toList();
    }

    @Override
    public String name() {
        return "NEAREST_FIRST";
    }
}
