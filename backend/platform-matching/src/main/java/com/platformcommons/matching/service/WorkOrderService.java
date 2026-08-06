package com.platformcommons.matching.service;

import com.platformcommons.matching.domain.OrderPriority;
import com.platformcommons.matching.domain.OrderTransition;
import com.platformcommons.matching.domain.OperatorRole;
import com.platformcommons.matching.domain.TransitionAction;
import com.platformcommons.matching.domain.WorkOrder;
import com.platformcommons.matching.domain.WorkOrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 业务工单服务。
 *
 * <p>封装工单全生命周期：创建、状态流转（带状态机校验）、查询、指派劳动者。</p>
 */
public interface WorkOrderService {

    /**
     * 创建工单。
     *
     * @param memberId    需求方会员 ID
     * @param orderType   工单类型
     * @param title       标题
     * @param description 描述
     * @param amount      金额
     * @param lat         纬度
     * @param lng         经度
     * @param scheduledAt 预约时间
     * @param priority    优先级
     * @return 新建的工单
     */
    WorkOrder createOrder(Long memberId, WorkOrderType orderType, String title, String description,
                          BigDecimal amount, Double lat, Double lng, Instant scheduledAt, OrderPriority priority);

    /**
     * 按订单号查询。
     */
    WorkOrder getOrder(String orderNo);

    /**
     * 按主键查询。
     */
    WorkOrder getOrder(Long orderId);

    /**
     * 列出需求方的工单（按创建时间倒序）。
     */
    List<WorkOrder> listMemberOrders(Long memberId);

    /**
     * 列出劳动者的工单（按创建时间倒序）。
     */
    List<WorkOrder> listWorkerOrders(Long workerId);

    /**
     * 工单状态流转。
     *
     * <p>执行状态机校验，记录流转日志，并更新相应时间戳。</p>
     *
     * @param orderId        工单 ID
     * @param action         流转动作
     * @param operatorId     操作人 ID
     * @param operatorRole   操作人角色
     * @param remark         备注
     * @param attachmentUrls 附件 URL
     * @return 流转后的工单
     */
    WorkOrder transitionOrder(Long orderId, TransitionAction action, Long operatorId,
                              OperatorRole operatorRole, String remark, String attachmentUrls);

    /**
     * 查询工单的流转历史。
     */
    List<OrderTransition> getOrderHistory(Long orderId);

    /**
     * 为工单指派劳动者。
     *
     * @param orderId  工单 ID
     * @param workerId 劳动者 ID
     * @return 更新后的工单
     */
    WorkOrder assignWorker(Long orderId, Long workerId);
}
