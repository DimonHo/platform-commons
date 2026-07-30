package com.platformcommons.governance.service;

import com.platformcommons.governance.api.dto.CreateProposalRequest;
import com.platformcommons.governance.api.dto.VoteRequest;
import com.platformcommons.governance.api.dto.VoteResultResponse;
import com.platformcommons.governance.domain.Proposal;

/**
 * 治理服务接口。
 *
 * <p>阿里规范要求 Service 层面向接口编程，由 {@code impl} 包下实现类提供具体逻辑。</p>
 */
public interface GovernanceService {

    /**
     * 创建提案（初始状态为草稿）。
     *
     * @param request 创建请求
     * @return 提案领域模型
     */
    Proposal createProposal(CreateProposalRequest request);

    /**
     * 开启提案投票（状态从 DRAFT → OPEN）。
     *
     * @param proposalId 提案 ID
     * @param durationHours 投票持续时长（小时）
     * @return 更新后的提案
     */
    Proposal startVoting(Long proposalId, int durationHours);

    /**
     * 成员对提案投票。
     *
     * @param proposalId 提案 ID
     * @param request    投票请求
     * @return 投票后的提案
     */
    Proposal castVote(Long proposalId, VoteRequest request);

    /**
     * 统计提案投票结果。
     *
     * @param proposalId 提案 ID
     * @return 投票统计结果
     */
    VoteResultResponse tallyResult(Long proposalId);

    /**
     * 查询提案详情。
     *
     * @param proposalId 提案 ID
     * @return 提案领域模型
     */
    Proposal getProposal(Long proposalId);
}
