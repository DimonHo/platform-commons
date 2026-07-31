package com.platformcommons.mutual.service.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.mutual.domain.ClaimStatus;
import com.platformcommons.mutual.domain.EligibilityResult;
import com.platformcommons.mutual.domain.LaborThreshold;
import com.platformcommons.mutual.domain.MutualClaim;
import com.platformcommons.mutual.repository.MutualClaimRepository;
import com.platformcommons.mutual.repository.entity.MutualClaimEntity;
import com.platformcommons.mutual.service.MutualFundService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 互助保障基金服务实现。
 *
 * <p>阿里规范：{@code @Override} 不省略；包装类比较使用 {@code equals()} 或 {@code compareTo()==0}。
 */
@Service
@Slf4j
public class MutualFundServiceImpl implements MutualFundService {


    /** 基础保障金额上限。 */
    private static final BigDecimal BASE_GUARANTEE_CAP = new BigDecimal("5000");
    /** 增强保障金额上限。 */
    private static final BigDecimal ENHANCED_GUARANTEE_CAP = new BigDecimal("20000");
    /** 单笔理赔申请金额上限（反欺诈）。 */
    private static final BigDecimal CLAIM_AMOUNT_CEILING = new BigDecimal("50000");

    /** 工种门槛映射。 */
    private static final Map<String, LaborThreshold> THRESHOLDS = Map.of(
            LaborThreshold.DELIVERY.jobCategory(), LaborThreshold.DELIVERY,
            LaborThreshold.CARE.jobCategory(), LaborThreshold.CARE,
            LaborThreshold.CRAFT.jobCategory(), LaborThreshold.CRAFT
    );

    /** 待处理状态列表（反欺诈用）。 */
    private static final List<String> ACTIVE_STATUSES = List.of(
            ClaimStatus.PENDING.name(), ClaimStatus.INVESTIGATING.name());

    private final MutualClaimRepository claimRepository;

    /** 理赔申请内存存储（演示用）。 */
    private final Map<UUID, MutualClaim> claimStore = new ConcurrentHashMap<>();

    public MutualFundServiceImpl(MutualClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public EligibilityResult assessEligibility(String applicantId, String jobCategory,
                                               BigDecimal monthlyHours, BigDecimal qualityScore,
                                               BigDecimal contributionScore) {
        Objects.requireNonNull(applicantId, "applicantId must not be null");
        Objects.requireNonNull(jobCategory, "jobCategory must not be null");

        LaborThreshold threshold = THRESHOLDS.getOrDefault(jobCategory, LaborThreshold.DELIVERY);

        boolean h0 = monthlyHours != null && monthlyHours.compareTo(threshold.h0()) >= 0;
        boolean q0 = qualityScore != null && qualityScore.compareTo(threshold.q0()) >= 0;
        boolean d0 = contributionScore != null && contributionScore.compareTo(threshold.d0()) >= 0;

        boolean base = h0;
        boolean enhanced = h0 && q0 && d0;

        BigDecimal cap = enhanced ? ENHANCED_GUARANTEE_CAP : (base ? BASE_GUARANTEE_CAP : BigDecimal.ZERO);

        String reason = null;
        if (!base) {
            reason = "H0 not satisfied: monthlyHours=" + monthlyHours + " < " + threshold.h0();
        } else if (!enhanced) {
            reason = "enhanced gate not met: q0=" + q0 + ", d0=" + d0;
        }

        log.info("Eligibility assessed: applicant={}, base={}, enhanced={}, cap={}",
                applicantId, base, enhanced, cap);

        return new EligibilityResult(applicantId, base, enhanced, h0, q0, d0, cap, reason);
    }

    @Override
    public MutualClaim submitClaim(String applicantId, String incidentType, String description,
                                   BigDecimal claimedAmount, List<String> evidenceUrls) {
        Objects.requireNonNull(applicantId, "applicantId must not be null");
        Objects.requireNonNull(incidentType, "incidentType must not be null");
        Objects.requireNonNull(claimedAmount, "claimedAmount must not be null");
        if (claimedAmount.signum() <= 0) {
            throw new BusinessException("claimedAmount must be positive: " + claimedAmount);
        }
        if (claimedAmount.compareTo(CLAIM_AMOUNT_CEILING) > 0) {
            throw new BusinessException("claimedAmount exceeds ceiling: " + claimedAmount);
        }
        if (evidenceUrls == null || evidenceUrls.isEmpty()) {
            throw new BusinessException("evidenceUrls must not be empty");
        }

        if (!antiFraudCheck(applicantId, claimedAmount)) {
            throw new BusinessException("anti-fraud check failed for applicant: " + applicantId);
        }

        UUID claimId = UUID.randomUUID();
        Instant now = Instant.now();
        MutualClaim claim = new MutualClaim(
                claimId, applicantId, incidentType, description, claimedAmount,
                List.copyOf(evidenceUrls), ClaimStatus.PENDING, now, null, null
        );
        claimStore.put(claimId, claim);

        MutualClaimEntity entity = toEntity(claim);
        claimRepository.save(entity);

        log.info("Claim submitted: claimId={}, applicant={}, amount={}", claimId, applicantId, claimedAmount);
        return claim;
    }

    @Override
    public boolean antiFraudCheck(String applicantId, BigDecimal claimedAmount) {
        Objects.requireNonNull(applicantId, "applicantId must not be null");
        long activeCount = claimRepository.countByApplicantIdAndStatusIn(applicantId, ACTIVE_STATUSES);
        if (activeCount > 0) {
            log.warn("Anti-fraud: applicant {} has {} active claims", applicantId, activeCount);
            return false;
        }
        if (claimedAmount != null && claimedAmount.compareTo(CLAIM_AMOUNT_CEILING) > 0) {
            log.warn("Anti-fraud: applicant {} claimedAmount {} exceeds ceiling", applicantId, claimedAmount);
            return false;
        }
        return true;
    }

    @Override
    public MutualClaim reviewClaim(UUID claimId, boolean approved, String reviewerId) {
        MutualClaim claim = claimStore.get(claimId);
        if (claim == null) {
            throw new BusinessException("claim not found: " + claimId);
        }
        if (!ClaimStatus.PENDING.equals(claim.status())) {
            throw new BusinessException("claim is not in PENDING state: " + claim.status());
        }

        Instant now = Instant.now();
        ClaimStatus newStatus = approved ? ClaimStatus.APPROVED : ClaimStatus.REJECTED;
        MutualClaim reviewed = new MutualClaim(
                claim.id(), claim.applicantId(), claim.incidentType(), claim.description(),
                claim.claimedAmount(), claim.evidenceUrls(), newStatus,
                claim.submittedAt(), now, reviewerId
        );
        claimStore.put(claimId, reviewed);

        claimRepository.save(toEntity(reviewed));
        log.info("Claim reviewed: claimId={}, approved={}, reviewer={}", claimId, approved, reviewerId);
        return reviewed;
    }

    @Override
    public Optional<MutualClaim> findById(UUID claimId) {
        return Optional.ofNullable(claimStore.get(claimId));
    }

    private static MutualClaimEntity toEntity(MutualClaim c) {
        MutualClaimEntity e = new MutualClaimEntity();
        e.setId(c.id());
        e.setApplicantId(c.applicantId());
        e.setIncidentType(c.incidentType());
        e.setDescription(c.description());
        e.setClaimedAmount(c.claimedAmount());
        e.setEvidenceUrls(c.evidenceUrls() == null ? null : String.join(",", c.evidenceUrls()));
        e.setStatus(c.status().name());
        e.setSubmittedAt(c.submittedAt());
        e.setReviewedAt(c.reviewedAt());
        e.setReviewerId(c.reviewerId());
        return e;
    }
}
