package com.platformcommons.identity.api.dto;

import java.time.Instant;

/**
 * 实名认证响应 DTO。
 */
public record IdentityVerificationResponse(
        Long id,
        Long memberId,
        String realName,
        String idCardType,
        String idCardNoMasked,
        String status,
        String verificationChannel,
        Boolean faceVerified,
        Instant submittedAt,
        Instant reviewedAt,
        Long reviewerId
) {
}
