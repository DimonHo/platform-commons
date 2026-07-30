package com.platformcommons.mutual.api;

import com.platformcommons.common.Result;
import com.platformcommons.mutual.api.dto.EligibilityResponse;
import com.platformcommons.mutual.api.dto.SubmitClaimRequest;
import com.platformcommons.mutual.domain.EligibilityResult;
import com.platformcommons.mutual.domain.MutualClaim;
import com.platformcommons.mutual.service.MutualFundService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 互助保障基金接口。
 */
@RestController
@RequestMapping("/api/mutual")
public class MutualFundController {

    private static final Logger log = LoggerFactory.getLogger(MutualFundController.class);

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
    public Result<EligibilityResponse> assess(
            @RequestParam String applicantId,
            @RequestParam String jobCategory,
            @RequestParam BigDecimal monthlyHours,
            @RequestParam BigDecimal qualityScore,
            @RequestParam BigDecimal contributionScore) {
        log.info("Eligibility request: applicant={}, job={}", applicantId, jobCategory);
        EligibilityResult r = mutualFundService.assessEligibility(
                applicantId, jobCategory, monthlyHours, qualityScore, contributionScore);
        return Result.success(toResponse(r));
    }

    /**
     * 提交理赔申请。
     *
     * @param request 申请请求
     * @return 已提交的申请
     */
    @PostMapping("/claims")
    public Result<MutualClaim> submit(@Valid @RequestBody SubmitClaimRequest request) {
        log.info("Submit claim: applicant={}, type={}", request.applicantId(), request.incidentType());
        MutualClaim claim = mutualFundService.submitClaim(
                request.applicantId(), request.incidentType(), request.description(),
                request.claimedAmount(), request.evidenceUrls());
        return Result.success(claim);
    }

    /**
     * 审核理赔。
     *
     * @param claimId    申请 ID
     * @param approved   是否批准
     * @param reviewerId 审核人 ID
     * @return 审核后的申请
     */
    @PostMapping("/claims/{claimId}/review")
    public Result<MutualClaim> review(@PathVariable UUID claimId,
                                      @RequestParam boolean approved,
                                      @RequestParam String reviewerId) {
        log.info("Review claim: claimId={}, approved={}, reviewer={}", claimId, approved, reviewerId);
        return Result.success(mutualFundService.reviewClaim(claimId, approved, reviewerId));
    }

    /**
     * 查询理赔申请。
     *
     * @param claimId 申请 ID
     * @return 理赔申请
     */
    @GetMapping("/claims/{claimId}")
    public Result<MutualClaim> get(@PathVariable UUID claimId) {
        return mutualFundService.findById(claimId)
                .map(Result::success)
                .orElseGet(() -> Result.failure("claim not found: " + claimId));
    }

    private static EligibilityResponse toResponse(EligibilityResult r) {
        return new EligibilityResponse(
                r.applicantId(), r.baseGuaranteed(), r.enhancedEligible(),
                r.h0Satisfied(), r.q0Satisfied(), r.d0Satisfied(),
                r.eligibleAmount(), r.reason()
        );
    }
}
