package com.platformcommons.governance.api;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.platformcommons.governance.api.dto.CreateProposalRequest;
import com.platformcommons.governance.api.dto.VoteRequest;
import com.platformcommons.governance.api.dto.VoteResultResponse;
import com.platformcommons.governance.domain.Proposal;
import com.platformcommons.governance.service.GovernanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 治理对外接口。
 *
 * <p>方法返回裸领域对象，由 {@code GlobalResponseAdvice} 自动包装为 {@code R<T>}。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "治理", description = "提案发起、投票、表决结果（第4-5章 第16-26条）")
public class GovernanceController {

    private final GovernanceService governanceService;

    /**
     * 创建提案。
     */
    @PostMapping("/api/governance/proposals")
    public Proposal createProposal(@Valid @RequestBody CreateProposalRequest request) {
        log.info("收到创建提案请求：title={}", request.title());
        return governanceService.createProposal(request);
    }

    /**
     * 查询提案详情。
     */
    @GetMapping("/api/governance/proposals/{id}")
    public Proposal getProposal(@PathVariable Long id) {
        return governanceService.getProposal(id);
    }

    /**
     * 开启投票。
     *
     * @param id            提案 ID
     * @param durationHours 投票时长（小时），不传则默认 72 小时
     */
    @PostMapping("/api/governance/proposals/{id}/voting")
    public Proposal startVoting(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "0") int durationHours) {
        log.info("收到开启投票请求：proposalId={}, durationHours={}", id, durationHours);
        return governanceService.startVoting(id, durationHours);
    }

    /**
     * 投票。
     */
    @PostMapping("/api/governance/proposals/{id}/votes")
    public Proposal vote(@PathVariable Long id,
                         @Valid @RequestBody VoteRequest request) {
        log.info("收到投票请求：proposalId={}, voterId={}", id, request.voterId());
        return governanceService.castVote(id, request);
    }

    /**
     * 统计投票结果。
     */
    @GetMapping("/api/governance/proposals/{id}/result")
    public VoteResultResponse tallyResult(@PathVariable Long id) {
        return governanceService.tallyResult(id);
    }
}
