package com.platformcommons.mutual.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 互助理赔申请。
 *
 * <p>映射宪章第14章 81-92条：劳动意外互助保障基金，由劳动者互助池出资，
 * 经资格认定与反欺诈检查后发放。
 *
 * @param id            申请 ID
 * @param applicantId   申请人（劳动者） ID
 * @param incidentType  事故类型
 * @param description   事故描述
 * @param claimedAmount 申请理赔金额
 * @param evidenceUrls  证据材料 URL 列表
 * @param status        申请状态
 * @param submittedAt   提交时间
 * @param reviewedAt    审核完成时间
 * @param reviewerId    审核人 ID
 */
public record MutualClaim(
        UUID id,
        String applicantId,
        String incidentType,
        String description,
        java.math.BigDecimal claimedAmount,
        java.util.List<String> evidenceUrls,
        ClaimStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String reviewerId
) {
}
