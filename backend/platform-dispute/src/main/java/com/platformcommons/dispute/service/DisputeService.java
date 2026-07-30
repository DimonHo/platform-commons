package com.platformcommons.dispute.service;

import com.platformcommons.dispute.domain.Dispute;
import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.domain.DisputeStatus;

import java.util.List;
import java.util.Optional;

/**
 * 争议申诉服务（第15章 第93-96条）
 * <p>
 * 三级递进救济流程管理。
 */
public interface DisputeService {

    /**
     * 提交争议申诉
     *
     * @param filedBy     申诉人
     * @param subject     争议事由
     * @param description 详细描述
     * @return 争议编号
     */
    String fileDispute(String filedBy, String subject, String description);

    /**
     * 处理争议（给出裁决）
     *
     * @param disputeId  争议编号
     * @param resolution 裁决结果
     * @return 更新后的争议
     */
    Dispute resolveDispute(String disputeId, String resolution);

    /**
     * 上诉至上一级（升级救济层级）
     *
     * @param disputeId 争议编号
     * @return 更新后的争议
     */
    Dispute appeal(String disputeId);

    /**
     * 查询争议详情
     *
     * @param disputeId 争议编号
     * @return 争议
     */
    Optional<Dispute> getDispute(String disputeId);

    /**
     * 查询申诉人的所有争议
     *
     * @param filedBy 申诉人
     * @return 争议列表
     */
    List<Dispute> listDisputesByUser(String filedBy);

    /**
     * 列出所有争议
     *
     * @return 争议列表
     */
    List<Dispute> listAllDisputes();

    /**
     * 按层级查询争议
     *
     * @param level 救济层级
     * @return 争议列表
     */
    List<Dispute> listDisputesByLevel(DisputeLevel level);
}
