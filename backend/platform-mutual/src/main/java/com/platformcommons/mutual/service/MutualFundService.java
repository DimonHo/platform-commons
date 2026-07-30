package com.platformcommons.mutual.service;

import com.platformcommons.mutual.domain.EligibilityResult;
import com.platformcommons.mutual.domain.MutualClaim;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * 劳动意外互助保障基金服务接口。
 *
 * <p>阿里规范：对外能力以接口形式提供。
 */
public interface MutualFundService {

    /**
     * 资格认定：根据申请人近期劳动数据判断基础保障与增强保障资格。
     *
     * @param applicantId 申请人 ID
     * @param jobCategory 工种类别
     * @param monthlyHours 近 30 日劳动时长（小时）
     * @param qualityScore 近 30 日质量分
     * @param contributionScore 近 30 日贡献度
     * @return 资格认定结果
     */
    EligibilityResult assessEligibility(String applicantId, String jobCategory,
                                        BigDecimal monthlyHours, BigDecimal qualityScore,
                                        BigDecimal contributionScore);

    /**
     * 提交理赔申请（需先通过资格认定与反欺诈检查）。
     *
     * @param applicantId  申请人 ID
     * @param incidentType 事故类型
     * @param description  事故描述
     * @param claimedAmount 申请金额
     * @param evidenceUrls 证据 URL 列表
     * @return 已提交的理赔申请（状态为 PENDING）
     */
    MutualClaim submitClaim(String applicantId, String incidentType, String description,
                            BigDecimal claimedAmount, java.util.List<String> evidenceUrls);

    /**
     * 反欺诈检查：检测重复申请、金额异常、证据缺失等风险。
     *
     * @param applicantId 申请人 ID
     * @param claimedAmount 申请金额
     * @return 无欺诈风险返回 true；否则 false
     */
    boolean antiFraudCheck(String applicantId, BigDecimal claimedAmount);

    /**
     * 审核理赔（批准或拒绝）。
     *
     * @param claimId   申请 ID
     * @param approved  是否批准
     * @param reviewerId 审核人 ID
     * @return 审核后的申请
     */
    MutualClaim reviewClaim(UUID claimId, boolean approved, String reviewerId);

    /**
     * 查询理赔申请。
     *
     * @param claimId 申请 ID
     * @return 理赔申请（可能不存在）
     */
    Optional<MutualClaim> findById(UUID claimId);
}
