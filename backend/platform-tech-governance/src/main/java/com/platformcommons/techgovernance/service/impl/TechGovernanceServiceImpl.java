package com.platformcommons.techgovernance.service.impl;

import com.platformcommons.techgovernance.domain.AlgorithmSpec;
import com.platformcommons.techgovernance.domain.DeploymentRecord;
import com.platformcommons.techgovernance.domain.TechAlert;
import com.platformcommons.techgovernance.domain.VerificationStatus;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.techgovernance.repository.DeploymentRecordRepository;
import com.platformcommons.techgovernance.repository.entity.DeploymentRecordEntity;
import com.platformcommons.techgovernance.service.TechGovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 算法代码数据治理服务实现（第13章 第70-80条）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechGovernanceServiceImpl implements TechGovernanceService {


    private final DeploymentRecordRepository deploymentRecordRepository;
    private final List<AlgorithmSpec> algorithmSpecs = new CopyOnWriteArrayList<>();
    private final List<TechAlert> alerts = new CopyOnWriteArrayList<>();


    @Override
    @Transactional
    public VerificationStatus verifyDeployment(DeploymentRecord deployment) {
        log.info("核验部署记录: deploymentId={}, commitHash={}", deployment.deploymentId(), deployment.commitHash());

        DeploymentRecordEntity entity = new DeploymentRecordEntity();
        entity.setDeploymentId(deployment.deploymentId());
        entity.setCommitHash(deployment.commitHash());
        entity.setBuildArtifactHash(deployment.buildArtifactHash());
        entity.setConfigDigest(deployment.configDigest());
        entity.setDeployedBy(deployment.deployedBy());

        VerificationStatus status;
        if (!StringUtils.hasText(deployment.commitHash()) || !StringUtils.hasText(deployment.buildArtifactHash())) {
            status = VerificationStatus.UNVERIFIABLE;
            raiseAlert(deployment.deploymentId(), "UNVERIFIABLE", "提交哈希或构建哈希缺失", TechAlert.Severity.HIGH);
        } else if (!isValidHashFormat(deployment.commitHash())) {
            status = VerificationStatus.MISMATCH;
            raiseAlert(deployment.deploymentId(), "HASH_MISMATCH", "提交哈希格式不合法", TechAlert.Severity.CRITICAL);
        } else {
            status = VerificationStatus.VERIFIED;
            log.info("部署核验通过: deploymentId={}", deployment.deploymentId());
        }

        entity.setVerificationStatus(status);
        deploymentRecordRepository.save(entity);
        return status;
    }

    @Override
    @Transactional
    public String registerAlgorithmSpec(AlgorithmSpec spec) {
        String algorithmId = SnowflakeUtils.nextId();
        log.info("注册算法规格: algorithmId={}, name={}, version={}", algorithmId, spec.algorithmName(), spec.version());
        algorithmSpecs.add(spec);
        return algorithmId;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlgorithmSpec> getAlgorithmSpec(String algorithmId) {
        return algorithmSpecs.stream()
                .filter(s -> s.algorithmName().equalsIgnoreCase(algorithmId))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlgorithmSpec> listAllAlgorithmSpecs() {
        return new ArrayList<>(algorithmSpecs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechAlert> listAlerts() {
        return new ArrayList<>(alerts);
    }

    private void raiseAlert(String deploymentId, String alertType, String description, TechAlert.Severity severity) {
        TechAlert alert = new TechAlert(
                SnowflakeUtils.nextId(),
                alertType,
                "[" + deploymentId + "] " + description,
                Instant.now(),
                severity
        );
        alerts.add(alert);
        log.warn("技术红线告警: {}", alert);
    }

    private boolean isValidHashFormat(String hash) {
        return hash != null && (hash.length() == 40 || hash.length() == 64)
                && hash.matches("[0-9a-fA-F]+");
    }
}
