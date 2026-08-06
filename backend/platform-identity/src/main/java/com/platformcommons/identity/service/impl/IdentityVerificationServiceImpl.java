package com.platformcommons.identity.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.domain.IdentityVerification;
import com.platformcommons.identity.domain.VerificationStatus;
import com.platformcommons.identity.repository.IdentityVerificationRepository;
import com.platformcommons.identity.repository.entity.IdentityVerificationEntity;
import com.platformcommons.identity.service.IdentityVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * {@link IdentityVerificationService} 默认实现。
 *
 * <p>演示用途：证件号加密采用简单反转，脱敏保留前 4 位与后 4 位。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private static final String DEFAULT_CARD_TYPE = "ID_CARD";
    private static final String DEFAULT_CHANNEL = "MANUAL";

    private final IdentityVerificationRepository verificationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IdentityVerification submitVerification(Long memberId, String realName, String idCardType,
                                                   String idCardNo, String channel) {
        log.info("提交实名认证：memberId={}, realName={}, idCardType={}", memberId, realName, idCardType);
        verificationRepository.findByMemberId(memberId).ifPresent(existing -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "该成员已提交实名认证: memberId=" + memberId);
        });

        String cardType = (idCardType == null || idCardType.isBlank()) ? DEFAULT_CARD_TYPE : idCardType;
        String verificationChannel = (channel == null || channel.isBlank()) ? DEFAULT_CHANNEL : channel;

        IdentityVerificationEntity entity = new IdentityVerificationEntity();
        entity.setMemberId(memberId);
        entity.setRealName(realName);
        entity.setIdCardType(cardType);
        entity.setIdCardNoEnc(encryptIdCardNo(idCardNo));
        entity.setIdCardNoMasked(maskIdCardNo(idCardNo));
        entity.setStatus(VerificationStatus.PENDING.name());
        entity.setVerificationChannel(verificationChannel);
        entity.setFaceVerified(false);
        entity.setSubmittedAt(Instant.now());

        IdentityVerificationEntity saved = verificationRepository.save(entity);
        log.info("实名认证提交成功：id={}, memberId={}", saved.getId(), memberId);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IdentityVerification approveVerification(Long verificationId, Long reviewerId) {
        log.info("审核通过实名认证：verificationId={}, reviewerId={}", verificationId, reviewerId);
        IdentityVerificationEntity entity = requireVerification(verificationId);
        if (!VerificationStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "仅待核验状态可通过: " + entity.getStatus());
        }
        entity.setStatus(VerificationStatus.VERIFIED.name());
        entity.setReviewerId(reviewerId);
        entity.setReviewedAt(Instant.now());
        IdentityVerificationEntity saved = verificationRepository.save(entity);
        log.info("实名认证审核通过：id={}", verificationId);
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IdentityVerification rejectVerification(Long verificationId, Long reviewerId, String reason) {
        log.info("驳回实名认证：verificationId={}, reviewerId={}, reason={}", verificationId, reviewerId, reason);
        IdentityVerificationEntity entity = requireVerification(verificationId);
        if (!VerificationStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "仅待核验状态可驳回: " + entity.getStatus());
        }
        entity.setStatus(VerificationStatus.REJECTED.name());
        entity.setReviewerId(reviewerId);
        entity.setReviewedAt(Instant.now());
        IdentityVerificationEntity saved = verificationRepository.save(entity);
        log.info("实名认证已驳回：id={}", verificationId);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IdentityVerification> getVerification(Long memberId) {
        return verificationRepository.findByMemberId(memberId)
                .map(IdentityVerificationServiceImpl::toDomain);
    }

    // ===== 内部工具 =====

    private IdentityVerificationEntity requireVerification(Long verificationId) {
        return verificationRepository.findById(verificationId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "实名认证记录不存在: " + verificationId));
    }

    /**
     * 演示用途：证件号加密（简单反转）。
     */
    private static String encryptIdCardNo(String idCardNo) {
        if (idCardNo == null) {
            return null;
        }
        return new StringBuilder(idCardNo).reverse().toString();
    }

    /**
     * 证件号脱敏：保留前 4 位与后 4 位，中间以星号替代。
     */
    private static String maskIdCardNo(String idCardNo) {
        if (idCardNo == null) {
            return null;
        }
        if (idCardNo.length() <= 8) {
            return "****";
        }
        return idCardNo.substring(0, 4)
                + "*".repeat(idCardNo.length() - 8)
                + idCardNo.substring(idCardNo.length() - 4);
    }

    private static IdentityVerification toDomain(IdentityVerificationEntity entity) {
        return new IdentityVerification(
                entity.getId(),
                entity.getMemberId(),
                entity.getRealName(),
                entity.getIdCardType(),
                entity.getIdCardNoEnc(),
                entity.getIdCardNoMasked(),
                entity.getStatus(),
                entity.getVerificationChannel(),
                entity.getFaceVerified(),
                entity.getSubmittedAt(),
                entity.getReviewedAt(),
                entity.getReviewerId()
        );
    }
}
