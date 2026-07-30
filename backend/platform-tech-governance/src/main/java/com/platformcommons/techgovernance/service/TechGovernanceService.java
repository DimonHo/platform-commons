package com.platformcommons.techgovernance.service;

import com.platformcommons.techgovernance.domain.AlgorithmSpec;
import com.platformcommons.techgovernance.domain.DeploymentRecord;
import com.platformcommons.techgovernance.domain.TechAlert;
import com.platformcommons.techgovernance.domain.VerificationStatus;

import java.util.List;
import java.util.Optional;

/**
 * 算法代码数据治理服务（第13章 第70-80条）
 * <p>
 * 负责可复现构建核验、算法规格审计、技术红线监控。
 */
public interface TechGovernanceService {

    /**
     * 核验部署记录
     *
     * @param deployment 部署记录
     * @return 核验结果
     */
    VerificationStatus verifyDeployment(DeploymentRecord deployment);

    /**
     * 注册算法规格说明
     *
     * @param spec 算法规格
     * @return 注册后的算法ID
     */
    String registerAlgorithmSpec(AlgorithmSpec spec);

    /**
     * 查询算法规格
     *
     * @param algorithmId 算法ID
     * @return 算法规格
     */
    Optional<AlgorithmSpec> getAlgorithmSpec(String algorithmId);

    /**
     * 列出所有算法规格
     *
     * @return 算法规格列表
     */
    List<AlgorithmSpec> listAllAlgorithmSpecs();

    /**
     * 查询技术红线告警
     *
     * @return 告警列表
     */
    List<TechAlert> listAlerts();
}
