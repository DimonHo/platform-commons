package com.platformcommons.identity.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.identity.domain.MemberRoleRecord;
import com.platformcommons.identity.domain.MerchantProfile;
import com.platformcommons.identity.domain.RoleStatus;
import com.platformcommons.identity.domain.RoleType;
import com.platformcommons.identity.domain.ShopStatus;
import com.platformcommons.identity.domain.VehicleType;
import com.platformcommons.identity.domain.WorkerOnlineStatus;
import com.platformcommons.identity.domain.WorkerProfile;
import com.platformcommons.identity.repository.MerchantProfileRepository;
import com.platformcommons.identity.repository.MemberRoleRepository;
import com.platformcommons.identity.repository.WorkerProfileRepository;
import com.platformcommons.identity.repository.entity.MerchantProfileEntity;
import com.platformcommons.identity.repository.entity.MemberRoleEntity;
import com.platformcommons.identity.repository.entity.WorkerProfileEntity;
import com.platformcommons.identity.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * {@link ProfileService} 默认实现。
 *
 * <p>负责成员多身份角色管理、劳动者/商家档案管理。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final int DEFAULT_SERVICE_RADIUS_M = 5000;
    private static final int DEFAULT_MAX_CONCURRENT = 1;
    private static final double DEFAULT_RATING = 5.0;
    private static final int DEFAULT_TOTAL_COMPLETED = 0;
    private static final int DEFAULT_DELIVERY_RADIUS_M = 3000;

    private final MemberRoleRepository memberRoleRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final MerchantProfileRepository merchantProfileRepository;

    // ===== 角色管理 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberRoleRecord applyRole(Long memberId, String roleType) {
        log.info("申请角色：memberId={}, roleType={}", memberId, roleType);
        RoleType type = parseRoleType(roleType);

        memberRoleRepository.findByMemberIdAndRoleType(memberId, type).ifPresent(existing -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "该角色已申请: " + roleType);
        });

        Instant now = Instant.now();
        MemberRoleEntity entity = new MemberRoleEntity();
        entity.setMemberId(memberId);
        entity.setRoleType(type);
        entity.setStatus(RoleStatus.PENDING);
        entity.setAppliedAt(now);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        MemberRoleEntity saved = memberRoleRepository.save(entity);
        log.info("角色申请成功：id={}, memberId={}, roleType={}", saved.getId(), memberId, type);
        return toRoleRecord(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberRoleRecord activateRole(Long memberRoleId, Long reviewerId) {
        log.info("激活角色：memberRoleId={}, reviewerId={}", memberRoleId, reviewerId);
        MemberRoleEntity entity = requireRole(memberRoleId);
        if (entity.getStatus() != RoleStatus.PENDING && entity.getStatus() != RoleStatus.SUSPENDED) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "当前状态不允许激活: " + entity.getStatus());
        }
        entity.setStatus(RoleStatus.ACTIVE);
        entity.setActivatedAt(Instant.now());
        entity.setReviewerId(reviewerId);
        entity.setUpdatedAt(Instant.now());
        MemberRoleEntity saved = memberRoleRepository.save(entity);
        log.info("角色激活成功：id={}", memberRoleId);
        return toRoleRecord(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberRoleRecord suspendRole(Long memberRoleId, String reason, Long reviewerId) {
        log.info("暂停角色：memberRoleId={}, reason={}, reviewerId={}", memberRoleId, reason, reviewerId);
        MemberRoleEntity entity = requireRole(memberRoleId);
        if (entity.getStatus() != RoleStatus.ACTIVE) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "仅正常状态的角色可暂停: " + entity.getStatus());
        }
        entity.setStatus(RoleStatus.SUSPENDED);
        entity.setSuspendedAt(Instant.now());
        entity.setSuspendReason(reason);
        entity.setReviewerId(reviewerId);
        entity.setUpdatedAt(Instant.now());
        MemberRoleEntity saved = memberRoleRepository.save(entity);
        log.info("角色暂停成功：id={}", memberRoleId);
        return toRoleRecord(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberRoleRecord> listMemberRoles(Long memberId) {
        return memberRoleRepository.findByMemberId(memberId).stream()
                .map(ProfileServiceImpl::toRoleRecord)
                .toList();
    }

    // ===== 劳动者档案 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkerProfile registerWorkerProfile(WorkerProfile profile) {
        log.info("注册劳动者档案：memberId={}", profile.memberId());
        workerProfileRepository.findByMemberId(profile.memberId()).ifPresent(existing -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "劳动者档案已存在: memberId=" + profile.memberId());
        });

        WorkerProfileEntity entity = new WorkerProfileEntity();
        entity.setMemberId(profile.memberId());
        entity.setServiceCategories(profile.serviceCategories());
        entity.setServiceRadiusM(profile.serviceRadiusM() != null ? profile.serviceRadiusM() : DEFAULT_SERVICE_RADIUS_M);
        entity.setVehicleType(profile.vehicleType() != null ? profile.vehicleType() : VehicleType.NONE);
        entity.setVehiclePlate(profile.vehiclePlate());
        entity.setSkills(profile.skills());
        entity.setMaxConcurrent(profile.maxConcurrent() != null ? profile.maxConcurrent() : DEFAULT_MAX_CONCURRENT);
        entity.setRating(profile.rating() != null ? profile.rating() : DEFAULT_RATING);
        entity.setTotalCompleted(profile.totalCompleted() != null ? profile.totalCompleted() : DEFAULT_TOTAL_COMPLETED);
        entity.setOnlineStatus(profile.onlineStatus() != null ? profile.onlineStatus() : WorkerOnlineStatus.OFFLINE);
        entity.setBio(profile.bio());

        WorkerProfileEntity saved = workerProfileRepository.save(entity);
        log.info("劳动者档案注册成功：id={}, memberId={}", saved.getId(), profile.memberId());
        return toWorkerProfile(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerProfile getWorkerProfile(Long memberId) {
        return workerProfileRepository.findByMemberId(memberId)
                .map(ProfileServiceImpl::toWorkerProfile)
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkerProfile updateWorkerOnlineStatus(Long memberId, String status) {
        log.info("更新劳动者在线状态：memberId={}, status={}", memberId, status);
        WorkerOnlineStatus target = parseOnlineStatus(status);
        WorkerProfileEntity entity = workerProfileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "劳动者档案不存在: memberId=" + memberId));
        entity.setOnlineStatus(target);
        WorkerProfileEntity saved = workerProfileRepository.save(entity);
        log.info("在线状态更新成功：memberId={}, status={}", memberId, target);
        return toWorkerProfile(saved);
    }

    // ===== 商家档案 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantProfile registerMerchantProfile(MerchantProfile profile) {
        log.info("注册商家档案：memberId={}", profile.memberId());
        merchantProfileRepository.findByMemberId(profile.memberId()).ifPresent(existing -> {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "商家档案已存在: memberId=" + profile.memberId());
        });

        MerchantProfileEntity entity = new MerchantProfileEntity();
        entity.setMemberId(profile.memberId());
        entity.setShopName(profile.shopName());
        entity.setShopCategory(profile.shopCategory());
        entity.setBusinessLicense(profile.businessLicense());
        entity.setLicensePhotoUrl(profile.licensePhotoUrl());
        entity.setShopAddress(profile.shopAddress());
        entity.setShopLat(profile.shopLat());
        entity.setShopLng(profile.shopLng());
        entity.setBusinessHours(profile.businessHours());
        entity.setDeliveryRadiusM(profile.deliveryRadiusM() != null ? profile.deliveryRadiusM() : DEFAULT_DELIVERY_RADIUS_M);
        entity.setRating(profile.rating() != null ? profile.rating() : DEFAULT_RATING);
        entity.setShopStatus(profile.shopStatus() != null ? profile.shopStatus() : ShopStatus.CLOSED);

        MerchantProfileEntity saved = merchantProfileRepository.save(entity);
        log.info("商家档案注册成功：id={}, memberId={}", saved.getId(), profile.memberId());
        return toMerchantProfile(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantProfile getMerchantProfile(Long memberId) {
        return merchantProfileRepository.findByMemberId(memberId)
                .map(ProfileServiceImpl::toMerchantProfile)
                .orElse(null);
    }

    // ===== 内部工具 =====

    private MemberRoleEntity requireRole(Long memberRoleId) {
        return memberRoleRepository.findById(memberRoleId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "角色记录不存在: " + memberRoleId));
    }

    private static RoleType parseRoleType(String roleType) {
        try {
            return RoleType.valueOf(roleType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法角色类型: " + roleType);
        }
    }

    private static WorkerOnlineStatus parseOnlineStatus(String status) {
        try {
            return WorkerOnlineStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法在线状态: " + status);
        }
    }

    private static MemberRoleRecord toRoleRecord(MemberRoleEntity entity) {
        return new MemberRoleRecord(
                entity.getId(),
                entity.getMemberId(),
                entity.getRoleType(),
                entity.getStatus(),
                entity.getAppliedAt(),
                entity.getActivatedAt(),
                entity.getSuspendedAt(),
                entity.getSuspendReason(),
                entity.getReviewerId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private static WorkerProfile toWorkerProfile(WorkerProfileEntity entity) {
        return new WorkerProfile(
                entity.getId(),
                entity.getMemberId(),
                entity.getServiceCategories(),
                entity.getServiceRadiusM(),
                entity.getVehicleType(),
                entity.getVehiclePlate(),
                entity.getSkills(),
                entity.getMaxConcurrent(),
                entity.getRating(),
                entity.getTotalCompleted(),
                entity.getOnlineStatus(),
                entity.getBio()
        );
    }

    private static MerchantProfile toMerchantProfile(MerchantProfileEntity entity) {
        return new MerchantProfile(
                entity.getId(),
                entity.getMemberId(),
                entity.getShopName(),
                entity.getShopCategory(),
                entity.getBusinessLicense(),
                entity.getLicensePhotoUrl(),
                entity.getShopAddress(),
                entity.getShopLat(),
                entity.getShopLng(),
                entity.getBusinessHours(),
                entity.getDeliveryRadiusM(),
                entity.getRating(),
                entity.getShopStatus()
        );
    }
}
