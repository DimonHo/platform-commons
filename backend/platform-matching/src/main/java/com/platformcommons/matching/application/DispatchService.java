package com.platformcommons.matching.application;

import com.platformcommons.matching.domain.dispatch.BroadcastType;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcast;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecord;
import com.platformcommons.matching.domain.workorder.WorkOrderType;

import java.util.List;

/**
 * 派单广播服务。
 *
 * <p>封装抢单/系统指派两种派单模式：创建广播、劳动者抢单、广播查询。</p>
 */
public interface DispatchService {

    /**
     * 创建派单广播。
     *
     * @param orderId       关联工单 ID
     * @param orderType     工单类型
     * @param centerLat     中心纬度
     * @param centerLng     中心经度
     * @param radiusMeters  半径（米）
     * @param targetCount   目标抢单人数
     * @param broadcastType 广播类型
     * @return 新建的广播
     */
    DispatchBroadcast createBroadcast(Long orderId, WorkOrderType orderType, Double centerLat, Double centerLng,
                                      Integer radiusMeters, Integer targetCount, BroadcastType broadcastType);

    /**
     * 劳动者抢单。
     *
     * @param broadcastId 广播 ID
     * @param workerId    劳动者 ID
     * @param workerLat   劳动者纬度
     * @param workerLng   劳动者经度
     * @return 抢单记录
     */
    DispatchGrabRecord grabOrder(Long broadcastId, Long workerId, Double workerLat, Double workerLng);

    /**
     * 按广播号查询。
     */
    DispatchBroadcast getBroadcast(String broadcastNo);

    /**
     * 列出广播中的活跃广播。
     */
    List<DispatchBroadcast> listActiveBroadcasts();

    /**
     * 列出广播的抢单记录。
     */
    List<DispatchGrabRecord> listGrabRecords(Long broadcastId);
}
