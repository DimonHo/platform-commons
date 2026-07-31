package com.platformcommons.mutual.api;

import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.mutual.api.dto.EligibilityResponse;
import com.platformcommons.mutual.api.dto.SubmitClaimRequest;
import com.platformcommons.mutual.domain.EligibilityResult;
import com.platformcommons.mutual.domain.MutualClaim;
import com.platformcommons.mutual.service.MutualFundService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/**
 * 互助保障基金接口。
 *
 * <p>方法返回裸领域对象，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@RestController
@RequestMapping("/api/mutual")
@Slf4j
public class MutualFundController {


    private final MutualFundService mutualFundService;

    public MutualFundController(MutualFundService mutualFundService) {
        this.mutualFundService = mutualFundService;
    }

    /**
     * 资格认定。
     *
     * @param applicantId       申请人 ID
     * @param jobCategory       工种类别
     * @param monthlyHours      月度劳动时长
     * @param qualityScore      质量分
     * @param contributionScore 贡献度
     * @return 资格认定结果
     */
    @GetMapping("/eligibility")
    public EligibilityResponse assess(
            @RequestParam String applicantId,
            @RequestParam String jobCategory,
            @RequestParam BigDecimal monthlyHours,
            @RequestParam BigDecimal qualityScore,
            @RequestParam BigDecimal contributionScore) {
        log.info("Eligibility request: applicant={}, job={}", applicantId, jobCategory);
        EligibilityResult r = mutualFundService.assessEligibility(
                applicantId, jobCategory, monthlyHours, qualityScore, contributionScore);
        return RecordUtils.copy(r, EligibilityResponse.class);
    }

    /**
     * 提交理赔申请。
     *
     * @param request 申请请求
     * @return 已提交的申请
     */
    @PostMapping("/claims")
    public MutualClaim submit(@Valid @RequestBody SubmitClaimRequest request) {
        log.info("Submit claim: applicant={}, type={}", request.applicantId(), request.incidentType());
        return mutualFundService.submitClaim(
                request.applicantId(), request.incidentType(), request.description(),
                request.claimedAmount(), request.evidenceUrls());
    }

    /**
     * 复核理赔申请。
     *
     * @param claimId   理赔 ID
     * @param reviewerId 复核人 ID
     * @param approved   是否批准（true=批准，false=拒绝）
     * @return 复核后的理赔
     */
    @PostMapping("/claims/{claimId}/review")
    public MutualClaim review(@PathVariable UUID claimId,
                              @RequestParam String reviewerId,
                              @RequestParam boolean approved) {
        log.info("Review claim: claimId={}, reviewer={}, approved={}", claimId, reviewerId, approved);
        return mutualFundService.reviewClaim(claimId, approved, reviewerId);
    }

    /**
     * 查询理赔详情。
     *
     * @param claimId 理赔 ID
     * @return 理赔详情
     */
    @GetMapping("/claims/{claimId}")
    public MutualClaim get(@PathVariable UUID claimId) {
        return mutualFundService.findById(claimId)
                .orElseThrow(() -> new com.platformcommons.common.exception.BusinessException(
                        com.platformcommons.common.api.ResultCode.DATA_NOT_FOUND,
                        "理赔不存在: " + claimId));
    }

}
