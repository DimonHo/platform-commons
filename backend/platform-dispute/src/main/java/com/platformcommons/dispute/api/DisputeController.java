package com.platformcommons.dispute.api;

import com.platformcommons.dispute.api.dto.DisputeResponse;
import com.platformcommons.dispute.api.dto.FileDisputeRequest;
import com.platformcommons.dispute.domain.Dispute;
import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.service.DisputeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 申诉争议 Controller（第15章 第93-96条）
 */
@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private static final Logger log = LoggerFactory.getLogger(DisputeController.class);

    private final DisputeService disputeService;

    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    /**
     * 提交争议申诉
     */
    @PostMapping
    public ResponseEntity<DisputeResponse> fileDispute(@Valid @RequestBody FileDisputeRequest request) {
        log.info("收到争议申诉: filedBy={}, subject={}", request.filedBy(), request.subject());
        String disputeId = disputeService.fileDispute(request.filedBy(), request.subject(), request.description());
        Dispute dispute = disputeService.getDispute(disputeId).orElseThrow();
        return ResponseEntity.ok(toResponse(dispute));
    }

    /**
     * 查询争议详情
     */
    @GetMapping("/{disputeId}")
    public ResponseEntity<DisputeResponse> getDispute(@PathVariable String disputeId) {
        return disputeService.getDispute(disputeId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 上诉至上一级
     */
    @PostMapping("/{disputeId}/appeal")
    public ResponseEntity<DisputeResponse> appeal(@PathVariable String disputeId) {
        log.info("收到上诉请求: disputeId={}", disputeId);
        Dispute dispute = disputeService.appeal(disputeId);
        return ResponseEntity.ok(toResponse(dispute));
    }

    /**
     * 裁决争议
     */
    @PostMapping("/{disputeId}/resolve")
    public ResponseEntity<DisputeResponse> resolve(@PathVariable String disputeId, @RequestBody String resolution) {
        log.info("收到裁决请求: disputeId={}", disputeId);
        Dispute dispute = disputeService.resolveDispute(disputeId, resolution);
        return ResponseEntity.ok(toResponse(dispute));
    }

    /**
     * 按层级查询争议
     */
    @GetMapping
    public ResponseEntity<List<DisputeResponse>> listDisputes(
            @RequestParam(value = "level", required = false) DisputeLevel level,
            @RequestParam(value = "filedBy", required = false) String filedBy) {
        List<Dispute> disputes;
        if (level != null) {
            disputes = disputeService.listDisputesByLevel(level);
        } else if (filedBy != null) {
            disputes = disputeService.listDisputesByUser(filedBy);
        } else {
            disputes = disputeService.listAllDisputes();
        }
        return ResponseEntity.ok(disputes.stream().map(this::toResponse).toList());
    }

    private DisputeResponse toResponse(Dispute dispute) {
        return new DisputeResponse(
                dispute.disputeId(),
                dispute.filedBy(),
                dispute.subject(),
                dispute.description(),
                dispute.level(),
                dispute.status(),
                dispute.resolution(),
                dispute.filedAt()
        );
    }
}
