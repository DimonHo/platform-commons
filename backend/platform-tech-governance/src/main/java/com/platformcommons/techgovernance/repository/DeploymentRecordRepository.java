package com.platformcommons.techgovernance.repository;

import com.platformcommons.techgovernance.repository.entity.DeploymentRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 部署记录 Repository
 */
public interface DeploymentRecordRepository extends JpaRepository<DeploymentRecordEntity, Long> {

    /**
     * 根据部署编号查询
     *
     * @param deploymentId 部署编号
     * @return 部署记录
     */
    Optional<DeploymentRecordEntity> findByDeploymentId(String deploymentId);
}
