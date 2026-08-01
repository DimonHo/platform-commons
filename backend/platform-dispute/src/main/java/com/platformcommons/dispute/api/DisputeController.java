package com.platformcommons.dispute.api;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.dispute.api.dto.DisputeResponse;
import com.platformcommons.dispute.api.dto.FileDisputeRequest;
import com.platformcommons.dispute.domain.Dispute;
import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.service.DisputeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 申诉争议 Controller（第15章 第93-96条）
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    /**
     * 提交争议申诉
     */
    @PostMapping("/api/disputes")
    public DisputeResponse fileDispute(@Valid @RequestBody FileDisputeRequest request) {
        log.info("收到争议申诉: filedBy={}, subject={}", request.filedBy(), request.subject());
        String disputeId = disputeService.fileDispute(request.filedBy(), request.subject(), request.description());
        Dispute dispute = disputeService.getDispute(disputeId).orElseThrow();
        return RecordUtils.copy(dispute, DisputeResponse.class);
    }

    /**
     * 查询争议详情
     */
    @GetMapping("/api/disputes/{disputeId}")
    public DisputeResponse getDispute(@PathVariable String disputeId) {
        return disputeService.getDispute(disputeId)
                .map(d -> RecordUtils.copy(d, DisputeResponse.class))
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND,
                        "争议不存在: " + disputeId));
    }

    /**
     * 上诉至上一级
     */
    @PostMapping("/api/disputes/{disputeId}/appeal")
    public DisputeResponse appeal(@PathVariable String disputeId) {
        log.info("收到上诉请求: disputeId={}", disputeId);
        Dispute dispute = disputeService.appeal(disputeId);
        return RecordUtils.copy(dispute, DisputeResponse.class);
    }

    /**
     * 裁决争议
     */
    @PostMapping("/api/disputes/{disputeId}/resolve")
    public DisputeResponse resolve(@PathVariable String disputeId, @RequestBody String resolution) {
        log.info("收到裁决请求: disputeId={}", disputeId);
        Dispute dispute = disputeService.resolveDispute(disputeId, resolution);
        return RecordUtils.copy(dispute, DisputeResponse.class);
    }

    /**
     * 按层级查询争议
     */
    @GetMapping("/api/disputes")
    public List<DisputeResponse> listDisputes(
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
        return disputes.stream().map(d -> RecordUtils.copy(d, DisputeResponse.class)).toList();
    }
}
