package com.platformcommons.mutual.repository;

import com.platformcommons.mutual.domain.ClaimStatus;
import com.platformcommons.mutual.repository.entity.MutualClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 互助理赔申请 Repository。
 */
public interface MutualClaimRepository extends JpaRepository<MutualClaimEntity, UUID> {

    /**
     * 按申请人查询所有理赔申请（按提交时间降序）。
     *
     * @param applicantId 申请人 ID
     * @return 申请列表
     */
    List<MutualClaimEntity> findByApplicantIdOrderBySubmittedAtDesc(String applicantId);

    /**
     * 统计申请人处于待处理/调查中状态的申请数量（用于反欺诈重复检测）。
     *
     * @param applicantId 申请人 ID
     * @return 数量
     */
    long countByApplicantIdAndStatusIn(String applicantId, List<ClaimStatus> statuses);
}
