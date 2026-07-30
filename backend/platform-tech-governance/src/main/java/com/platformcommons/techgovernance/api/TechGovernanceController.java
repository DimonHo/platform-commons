package com.platformcommons.techgovernance.api;

import com.platformcommons.techgovernance.api.dto.AlgorithmSpecResponse;
import com.platformcommons.techgovernance.api.dto.DeploymentVerifyRequest;
import com.platformcommons.techgovernance.domain.AlgorithmSpec;
import com.platformcommons.techgovernance.domain.DeploymentRecord;
import com.platformcommons.techgovernance.domain.TechAlert;
import com.platformcommons.techgovernance.domain.VerificationStatus;
import com.platformcommons.techgovernance.service.TechGovernanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 算法代码数据治理 Controller（第13章 第70-80条）
 */
@RestController
@RequestMapping("/api/tech-governance")
public class TechGovernanceController {

    private static final Logger log = LoggerFactory.getLogger(TechGovernanceController.class);

    private final TechGovernanceService techGovernanceService;

    public TechGovernanceController(TechGovernanceService techGovernanceService) {
        this.techGovernanceService = techGovernanceService;
    }

    /**
     * 核验部署
     */
    @PostMapping("/deployments/verify")
    public ResponseEntity<String> verifyDeployment(@Valid @RequestBody DeploymentVerifyRequest request) {
        log.info("收到部署核验请求: commitHash={}", request.commitHash());
        String deploymentId = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        DeploymentRecord record = new DeploymentRecord(
                deploymentId,
                request.commitHash(),
                request.buildArtifactHash(),
                request.configDigest(),
                java.time.Instant.now().toString(),
                request.deployedBy()
        );
        VerificationStatus status = techGovernanceService.verifyDeployment(record);
        return ResponseEntity.ok(status.getDescription());
    }

    /**
     * 注册算法规格
     */
    @PostMapping("/algorithms")
    public ResponseEntity<String> registerAlgorithm(@RequestBody AlgorithmSpec spec) {
        log.info("收到算法规格注册: name={}, version={}", spec.algorithmName(), spec.version());
        String algorithmId = techGovernanceService.registerAlgorithmSpec(spec);
        return ResponseEntity.ok(algorithmId);
    }

    /**
     * 查询所有算法规格
     */
    @GetMapping("/algorithms")
    public ResponseEntity<List<AlgorithmSpecResponse>> listAlgorithms() {
        List<AlgorithmSpecResponse> responses = techGovernanceService.listAllAlgorithmSpecs().stream()
                .map(s -> new AlgorithmSpecResponse(
                        s.algorithmName(), s.algorithmName(), s.version(), s.objective(),
                        s.inputs(), s.weightRanges(), s.constraints(), s.isCritical()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 查询技术红线告警
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<TechAlert>> listAlerts() {
        return ResponseEntity.ok(techGovernanceService.listAlerts());
    }
}
