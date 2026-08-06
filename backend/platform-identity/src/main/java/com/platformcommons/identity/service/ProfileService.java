package com.platformcommons.identity.service;

import com.platformcommons.identity.domain.MemberRoleRecord;
import com.platformcommons.identity.domain.MerchantProfile;
import com.platformcommons.identity.domain.WorkerProfile;

import java.util.List;

/**
 * 多身份与档案服务接口。
 *
 * <p>涵盖成员多角色管理、劳动者档案、商家档案等功能。</p>
 */
public interface ProfileService {

    /**
     * 为成员申请角色。
     */
    MemberRoleRecord applyRole(Long memberId, String roleType);

    /**
     * 激活角色。
     */
    MemberRoleRecord activateRole(Long memberRoleId, Long reviewerId);

    /**
     * 暂停角色。
     */
    MemberRoleRecord suspendRole(Long memberRoleId, String reason, Long reviewerId);

    /**
     * 查询成员的全部角色。
     */
    List<MemberRoleRecord> listMemberRoles(Long memberId);

    /**
     * 注册劳动者档案。
     */
    WorkerProfile registerWorkerProfile(WorkerProfile profile);

    /**
     * 查询劳动者档案。
     */
    WorkerProfile getWorkerProfile(Long memberId);

    /**
     * 更新劳动者在线状态。
     */
    WorkerProfile updateWorkerOnlineStatus(Long memberId, String status);

    /**
     * 注册商家档案。
     */
    MerchantProfile registerMerchantProfile(MerchantProfile profile);

    /**
     * 查询商家档案。
     */
    MerchantProfile getMerchantProfile(Long memberId);
}
