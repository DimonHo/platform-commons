package com.platformcommons.identity.api;

import com.platformcommons.identity.api.dto.ApplyRoleRequest;
import com.platformcommons.identity.api.dto.MerchantProfileResponse;
import com.platformcommons.identity.api.dto.RegisterMerchantProfileRequest;
import com.platformcommons.identity.api.dto.RegisterWorkerProfileRequest;
import com.platformcommons.identity.api.dto.SuspendRoleRequest;
import com.platformcommons.identity.api.dto.WorkerProfileResponse;
import com.platformcommons.identity.domain.role.MemberRoleRecord;
import com.platformcommons.identity.domain.profile.MerchantProfile;
import com.platformcommons.identity.domain.profile.VehicleType;
import com.platformcommons.identity.domain.profile.WorkerOnlineStatus;
import com.platformcommons.identity.domain.profile.WorkerProfile;
import com.platformcommons.identity.application.ProfileService;
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

import java.util.List;

/**
 * 多身份与档案管理对外接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装为 {@code R<T>}。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "多身份管理", description = "成员多角色、劳动者档案、商家档案管理")
public class ProfileController {

    private final ProfileService profileService;

    // ===== 角色管理 =====

    /**
     * 申请角色。
     */
    @PostMapping("/api/members/{memberId}/roles")
    public MemberRoleRecord applyRole(@PathVariable Long memberId,
                                      @Valid @RequestBody ApplyRoleRequest request) {
        log.info("申请角色：memberId={}, roleType={}", memberId, request.roleType());
        return profileService.applyRole(memberId, request.roleType());
    }

    /**
     * 激活角色。
     */
    @PutMapping("/api/members/{memberId}/roles/{roleId}/activate")
    public MemberRoleRecord activateRole(@PathVariable Long memberId,
                                         @PathVariable Long roleId,
                                         @RequestParam Long reviewerId) {
        log.info("激活角色：memberId={}, roleId={}, reviewerId={}", memberId, roleId, reviewerId);
        return profileService.activateRole(roleId, reviewerId);
    }

    /**
     * 暂停角色。
     */
    @PutMapping("/api/members/{memberId}/roles/{roleId}/suspend")
    public MemberRoleRecord suspendRole(@PathVariable Long memberId,
                                        @PathVariable Long roleId,
                                        @RequestParam Long reviewerId,
                                        @Valid @RequestBody SuspendRoleRequest request) {
        log.info("暂停角色：memberId={}, roleId={}, reviewerId={}", memberId, roleId, reviewerId);
        return profileService.suspendRole(roleId, request.reason(), reviewerId);
    }

    /**
     * 查询成员全部角色。
     */
    @GetMapping("/api/members/{memberId}/roles")
    public List<MemberRoleRecord> listRoles(@PathVariable Long memberId) {
        return profileService.listMemberRoles(memberId);
    }

    // ===== 劳动者档案 =====

    /**
     * 注册劳动者档案。
     */
    @PostMapping("/api/members/{memberId}/worker-profile")
    public WorkerProfileResponse registerWorkerProfile(@PathVariable Long memberId,
                                                       @Valid @RequestBody RegisterWorkerProfileRequest request) {
        log.info("注册劳动者档案：memberId={}", memberId);
        WorkerProfile profile = new WorkerProfile(
                null,
                memberId,
                request.serviceCategories(),
                request.serviceRadiusM(),
                VehicleType.valueOf(request.vehicleType().toUpperCase()),
                request.vehiclePlate(),
                request.skills(),
                request.maxConcurrent(),
                null,
                null,
                null,
                request.bio()
        );
        WorkerProfile saved = profileService.registerWorkerProfile(profile);
        return toWorkerResponse(saved);
    }

    /**
     * 查询劳动者档案。
     */
    @GetMapping("/api/members/{memberId}/worker-profile")
    public WorkerProfileResponse getWorkerProfile(@PathVariable Long memberId) {
        WorkerProfile profile = profileService.getWorkerProfile(memberId);
        return profile == null ? null : toWorkerResponse(profile);
    }

    /**
     * 更新劳动者在线状态。
     */
    @PutMapping("/api/members/{memberId}/worker-profile/online-status")
    public WorkerProfileResponse updateOnlineStatus(@PathVariable Long memberId,
                                                    @RequestParam String status) {
        log.info("更新在线状态：memberId={}, status={}", memberId, status);
        WorkerProfile updated = profileService.updateWorkerOnlineStatus(memberId, status);
        return toWorkerResponse(updated);
    }

    // ===== 商家档案 =====

    /**
     * 注册商家档案。
     */
    @PostMapping("/api/members/{memberId}/merchant-profile")
    public MerchantProfileResponse registerMerchantProfile(@PathVariable Long memberId,
                                                           @Valid @RequestBody RegisterMerchantProfileRequest request) {
        log.info("注册商家档案：memberId={}", memberId);
        MerchantProfile profile = new MerchantProfile(
                null,
                memberId,
                request.shopName(),
                request.shopCategory(),
                request.businessLicense(),
                request.licensePhotoUrl(),
                request.shopAddress(),
                request.shopLat(),
                request.shopLng(),
                request.businessHours(),
                request.deliveryRadiusM(),
                null,
                null
        );
        MerchantProfile saved = profileService.registerMerchantProfile(profile);
        return toMerchantResponse(saved);
    }

    /**
     * 查询商家档案。
     */
    @GetMapping("/api/members/{memberId}/merchant-profile")
    public MerchantProfileResponse getMerchantProfile(@PathVariable Long memberId) {
        MerchantProfile profile = profileService.getMerchantProfile(memberId);
        return profile == null ? null : toMerchantResponse(profile);
    }

    // ===== 内部工具 =====

    private static WorkerProfileResponse toWorkerResponse(WorkerProfile p) {
        return new WorkerProfileResponse(
                p.id(), p.memberId(), p.serviceCategories(), p.serviceRadiusM(),
                p.vehicleType(), p.vehiclePlate(), p.skills(), p.maxConcurrent(),
                p.rating(), p.totalCompleted(), p.onlineStatus(), p.bio()
        );
    }

    private static MerchantProfileResponse toMerchantResponse(MerchantProfile p) {
        return new MerchantProfileResponse(
                p.id(), p.memberId(), p.shopName(), p.shopCategory(),
                p.businessLicense(), p.licensePhotoUrl(), p.shopAddress(),
                p.shopLat(), p.shopLng(), p.businessHours(),
                p.deliveryRadiusM(), p.rating(), p.shopStatus()
        );
    }
}
