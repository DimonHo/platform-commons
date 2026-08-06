package com.platformcommons.identity.repository;

import com.platformcommons.identity.repository.entity.WorkerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 劳动者档案 Repository。
 */
public interface WorkerProfileRepository extends JpaRepository<WorkerProfileEntity, Long> {

    /**
     * 根据成员 ID 查询劳动者档案。
     */
    Optional<WorkerProfileEntity> findByMemberId(Long memberId);
}
