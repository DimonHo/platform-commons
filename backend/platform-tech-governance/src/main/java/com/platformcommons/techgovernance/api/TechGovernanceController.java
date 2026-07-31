package com.platformcommons.techgovernance.api;

import com.platformcommons.techgovernance.api.dto.DeploymentVerifyRequest;
import com.platformcommons.techgovernance.domain.AlgorithmSpec;
import com.platformcommons.techgovernance.domain.DeploymentRecord;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.techgovernance.domain.TechAlert;
import com.platformcommons.techgovernance.domain.VerificationStatus;
import com.platformcommons.techgovernance.service.TechGovernanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 算法代码数据治理 Controller（第13章 第70-80条）
 *
 * <p>方法返回裸对象，由 {@code GlobalResponseAdvice} 自动包装。
 * 原本返回 String 的方法改为返回 {@code Map}（等价的简单 POJO 亦可），
 * 避免 String 返回被 Spring MVC 的 StringHttpMessageConverter 特殊处理。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tech-governance")
public class TechGovernanceController {


    private final TechGovernanceService techGovernanceService;


    /**
     * 核验部署
     */
    @PostMapping("/deployments/verify")
    public Map<String, String> verifyDeployment(@Valid @RequestBody DeploymentVerifyRequest request) {
        log.info("收到部署核验请求: commitHash={}", request.commitHash());
        String deploymentId = SnowflakeUtils.nextId();
        DeploymentRecord record = new DeploymentRecord(
                deploymentId,
                request.commitHash(),
                request.buildArtifactHash(),
                request.configDigest(),
                Instant.now().toString(),
                request.deployedBy()
        );
        VerificationStatus status = techGovernanceService.verifyDeployment(record);
        return Map.of("deploymentId", deploymentId, "status", status.getDescription());
    }

    /**
     * 注册算法规格
     */
    @PostMapping("/algorithms")
    public Map<String, String> registerAlgorithm(@RequestBody AlgorithmSpec spec) {
        log.info("收到算法规格注册: name={}, version={}", spec.algorithmName(), spec.version());
        String algorithmId = techGovernanceService.registerAlgorithmSpec(spec);
        return Map.of("algorithmId", algorithmId);
    }

    /**
     * 查询所有算法规格
     */
    @GetMapping("/algorithms")
    public List<AlgorithmSpec> listAlgorithms() {
        return techGovernanceService.listAllAlgorithmSpecs();
    }

    /**
     * 查询所有告警
     */
    @GetMapping("/alerts")
    public List<TechAlert> listAlerts() {
        return techGovernanceService.listAlerts();
    }
}
