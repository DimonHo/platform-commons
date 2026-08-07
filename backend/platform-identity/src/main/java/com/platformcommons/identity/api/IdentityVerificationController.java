package com.platformcommons.identity.api;

import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.identity.api.dto.IdentityVerificationResponse;
import com.platformcommons.identity.api.dto.SubmitVerificationRequest;
import com.platformcommons.identity.domain.verification.IdentityVerification;
import com.platformcommons.identity.application.IdentityVerificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实名认证对外接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "实名认证", description = "实名认证提交与审核")
public class IdentityVerificationController {

    private final IdentityVerificationService verificationService;

    /**
     * 提交实名认证。
     */
    @PostMapping("/api/members/{memberId}/identity-verification")
    public IdentityVerificationResponse submitVerification(@PathVariable Long memberId,
                                                           @Valid @RequestBody SubmitVerificationRequest request) {
        log.info("提交实名认证：memberId={}", memberId);
        IdentityVerification verification = verificationService.submitVerification(
                memberId,
                request.realName(),
                request.idCardType(),
                request.idCardNo(),
                request.verificationChannel()
        );
        return toResponse(verification);
    }

    /**
     * 审核通过。
     */
    @PutMapping("/api/identity-verifications/{verificationId}/approve")
    public IdentityVerificationResponse approveVerification(@PathVariable Long verificationId,
                                                            @RequestParam Long reviewerId) {
        log.info("审核通过：verificationId={}, reviewerId={}", verificationId, reviewerId);
        IdentityVerification verification = verificationService.approveVerification(verificationId, reviewerId);
        return toResponse(verification);
    }

    /**
     * 审核驳回。
     */
    @PutMapping("/api/identity-verifications/{verificationId}/reject")
    public IdentityVerificationResponse rejectVerification(@PathVariable Long verificationId,
                                                           @RequestParam Long reviewerId,
                                                           @RequestBody(required = false) String reason) {
        log.info("审核驳回：verificationId={}, reviewerId={}", verificationId, reviewerId);
        IdentityVerification verification = verificationService.rejectVerification(
                verificationId, reviewerId, reason == null ? "" : reason
        );
        return toResponse(verification);
    }

    /**
     * 查询成员的实名认证。
     */
    @GetMapping("/api/members/{memberId}/identity-verification")
    public IdentityVerificationResponse getVerification(@PathVariable Long memberId) {
        return verificationService.getVerification(memberId)
                .map(IdentityVerificationController::toResponse)
                .orElse(null);
    }

    private static IdentityVerificationResponse toResponse(IdentityVerification v) {
        return RecordUtils.copy(v, IdentityVerificationResponse.class);
    }
}
