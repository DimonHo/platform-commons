package com.platformcommons.governance.api;

import com.platformcommons.governance.api.dto.CreateProposalRequest;
import com.platformcommons.governance.api.dto.VoteRequest;
import com.platformcommons.governance.api.dto.VoteResultResponse;
import com.platformcommons.governance.domain.Proposal;
import com.platformcommons.governance.service.GovernanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 治理对外接口。
 *
 * <p>方法返回裸领域对象，由 {@code GlobalResponseAdvice} 自动包装为 {@code R<T>}。</p>
 */
@RestController
@RequestMapping("/api/governance/proposals")
public class GovernanceController {

    private static final Logger log = LoggerFactory.getLogger(GovernanceController.class);

    private final GovernanceService governanceService;

    public GovernanceController(GovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    /**
     * 创建提案。
     */
    @PostMapping
    public Proposal createProposal(@Valid @RequestBody CreateProposalRequest request) {
        log.info("收到创建提案请求：title={}", request.title());
        return governanceService.createProposal(request);
    }

    /**
     * 查询提案详情。
     */
    @GetMapping("/{id}")
    public Proposal getProposal(@PathVariable Long id) {
        return governanceService.getProposal(id);
    }

    /**
     * 开启投票。
     *
     * @param id            提案 ID
     * @param durationHours 投票时长（小时），不传则默认 72 小时
     */
    @PostMapping("/{id}/voting")
    public Proposal startVoting(@PathVariable Long id,
                                @RequestParam(required = false, defaultValue = "0") int durationHours) {
        log.info("收到开启投票请求：proposalId={}, durationHours={}", id, durationHours);
        return governanceService.startVoting(id, durationHours);
    }

    /**
     * 投票。
     */
    @PostMapping("/{id}/votes")
    public Proposal vote(@PathVariable Long id,
                         @Valid @RequestBody VoteRequest request) {
        log.info("收到投票请求：proposalId={}, voterId={}", id, request.voterId());
        return governanceService.castVote(id, request);
    }

    /**
     * 统计投票结果。
     */
    @GetMapping("/{id}/result")
    public VoteResultResponse tallyResult(@PathVariable Long id) {
        return governanceService.tallyResult(id);
    }
}
