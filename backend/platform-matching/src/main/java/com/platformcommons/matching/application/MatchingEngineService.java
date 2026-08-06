package com.platformcommons.matching.application;

import com.platformcommons.matching.domain.match.MatchResult;

import java.util.List;

/**
 * 匹配引擎服务接口。
 */
public interface MatchingEngineService {

    /**
     * 执行匹配：根据任务位置和候选劳动者，按指定策略排序并应用反榨取约束。
     *
     * @param taskId      任务 ID
     * @param strategyName 策略名称（NEAREST_FIRST / FAIR_ROUND_ROBIN）
     * @return 匹配结果
     */
    MatchResult match(String taskId, String strategyName);

    /**
     * 注册劳动者位置（用于匹配候选）。
     *
     * @param workerId       劳动者 ID
     * @param latitude       纬度
     * @param longitude      经度
     * @param activeOrders   当前活跃订单数
     * @param rating         综合评分
     * @param registrationDays 注册天数
     */
    void registerWorker(String workerId, double latitude, double longitude,
                        int activeOrders, double rating, int registrationDays);

    /**
     * 列出所有已注册的候选劳动者。
     *
     * @return 劳动者 ID 列表
     */
    List<String> listWorkers();
}
