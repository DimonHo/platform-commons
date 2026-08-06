package com.platformcommons.identity.application;

import com.platformcommons.identity.domain.verification.IdentityVerification;

import java.util.Optional;

/**
 * 实名认证服务接口。
 */
public interface IdentityVerificationService {

    /**
     * 提交实名认证申请。
     */
    IdentityVerification submitVerification(Long memberId, String realName, String idCardType,
                                           String idCardNo, String channel);

    /**
     * 审核通过。
     */
    IdentityVerification approveVerification(Long verificationId, Long reviewerId);

    /**
     * 审核驳回。
     */
    IdentityVerification rejectVerification(Long verificationId, Long reviewerId, String reason);

    /**
     * 查询成员的实名认证记录。
     */
    Optional<IdentityVerification> getVerification(Long memberId);
}
