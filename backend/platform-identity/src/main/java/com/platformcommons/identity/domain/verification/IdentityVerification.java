package com.platformcommons.identity.domain.verification;

import java.time.Instant;

/**
 * 实名认证记录领域模型（不可变 record）。
 *
 * @param id                 主键
 * @param memberId           成员 ID
 * @param realName           真实姓名
 * @param idCardType         证件类型
 * @param idCardNoEnc        证件号密文（演示用反转）
 * @param idCardNoMasked     证件号脱敏
 * @param status             核验状态
 * @param verificationChannel 核验渠道
 * @param faceVerified       人脸核验结果
 * @param submittedAt        提交时间
 * @param reviewedAt         审核时间
 * @param reviewerId         审核员 ID
 */
public record IdentityVerification(
        Long id,
        Long memberId,
        String realName,
        String idCardType,
        String idCardNoEnc,
        String idCardNoMasked,
        String status,
        String verificationChannel,
        Boolean faceVerified,
        Instant submittedAt,
        Instant reviewedAt,
        Long reviewerId
) {
}
