package com.platformcommons.matching.strategy;

import com.platformcommons.matching.domain.AntiExploitationConfig;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 匹配策略 sealed interface。
 *
 * <p>阿里规范：策略模式以接口形式定义；sealed 限定允许的实现，保证可解释性。
 * 仅允许 {@link NearestFirstStrategy} 与 {@link FairRoundRobinStrategy}。
 */
public sealed interface MatchingStrategy
        permits NearestFirstStrategy, FairRoundRobinStrategy {

    /**
     * 对候选劳动者进行排序。
     *
     * @param candidates 候选劳动者列表
     * @param config     反榨取约束
     * @return 排序后的劳动者 ID 列表
     */
    List<String> rank(List<WorkerCandidate> candidates, AntiExploitationConfig config);

    /**
     * 策略名称。
     *
     * @return 名称
     */
    String name();

    /**
     * 候选劳动者信息。
     *
     * @param workerId       劳动者 ID
     * @param distanceKm     距任务点距离（公里）
     * @param activeOrders   当前活跃订单数
     * @param rating         综合评分（0-5）
     * @param registrationDays 注册天数（用于新人保护判定）
     */
    record WorkerCandidate(
            String workerId,
            double distanceKm,
            int activeOrders,
            double rating,
            int registrationDays
    ) implements Comparable<WorkerCandidate> {

        /** 比较器：按距离升序。 */
        public static final Comparator<WorkerCandidate> BY_DISTANCE =
                Comparator.comparingDouble(WorkerCandidate::distanceKm);

        /** 比较器：按活跃订单数升序（公平）。 */
        public static final Comparator<WorkerCandidate> BY_ACTIVE_ORDERS =
                Comparator.comparingInt(WorkerCandidate::activeOrders);

        @Override
        public int compareTo(WorkerCandidate o) {
            return BY_DISTANCE.thenComparing(BY_ACTIVE_ORDERS).compare(this, o);
        }
    }
}
