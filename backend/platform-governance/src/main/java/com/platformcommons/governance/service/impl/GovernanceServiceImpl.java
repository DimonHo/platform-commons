package com.platformcommons.governance.service.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.governance.api.dto.CreateProposalRequest;
import com.platformcommons.governance.api.dto.VoteRequest;
import com.platformcommons.governance.api.dto.VoteResultResponse;
import com.platformcommons.governance.domain.Proposal;
import com.platformcommons.governance.domain.ProposalStatus;
import com.platformcommons.governance.domain.ProposalType;
import com.platformcommons.governance.domain.GovernanceChamber;
import com.platformcommons.governance.domain.VoteChoice;
import com.platformcommons.governance.repository.entity.VoteEntity;
import com.platformcommons.governance.repository.ProposalRepository;
import com.platformcommons.governance.repository.VoteRepository;
import com.platformcommons.governance.repository.entity.ProposalEntity;
import com.platformcommons.governance.service.GovernanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * {@link GovernanceService} 默认实现。
 *
 * <p>负责提案创建、投票开启、投票记录与结果统计。
 * 日志统一使用 SLF4J，占位符拼接。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GovernanceServiceImpl implements GovernanceService {


    /** 默认投票持续时长（小时） */
    private static final int DEFAULT_VOTING_DURATION_HOURS = 72;
    /** 最小投票持续时长（小时） */
    private static final int MIN_VOTING_DURATION_HOURS = 1;

    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Proposal createProposal(CreateProposalRequest request) {
        log.info("创建提案：title={}, type={}", request.title(), request.type());

        ProposalType type = parseType(request.type());
        GovernanceChamber chamber = parseChamber(request.targetChamber());

        ProposalEntity entity = new ProposalEntity();
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setType(type);
        entity.setStatus(ProposalStatus.DRAFT);
        entity.setProposerId(request.proposerId());
        entity.setTargetChamber(chamber);
        entity.setCreatedAt(LocalDateTime.now());

        ProposalEntity saved = proposalRepository.save(entity);
        log.info("提案创建成功：id={}", saved.getId());
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Proposal startVoting(Long proposalId, int durationHours) {
        log.info("开启投票：proposalId={}, durationHours={}", proposalId, durationHours);
        int effectiveDuration = durationHours <= 0 ? DEFAULT_VOTING_DURATION_HOURS : durationHours;
        if (effectiveDuration < MIN_VOTING_DURATION_HOURS) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "投票时长至少 1 小时");
        }

        ProposalEntity entity = requireProposal(proposalId);
        if (entity.getStatus() != ProposalStatus.DRAFT) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED,
                    "仅草稿状态提案可开启投票，当前状态: " + entity.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(ProposalStatus.OPEN);
        entity.setVotingStartAt(now);
        entity.setVotingEndAt(now.plusHours(effectiveDuration));
        ProposalEntity saved = proposalRepository.save(entity);
        log.info("投票已开启：proposalId={}, endAt={}", proposalId, saved.getVotingEndAt());
        return toDomain(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Proposal castVote(Long proposalId, VoteRequest request) {
        log.info("投票：proposalId={}, voterId={}, choice={}", proposalId, request.voterId(), request.choice());

        ProposalEntity entity = requireProposal(proposalId);
        if (entity.getStatus() != ProposalStatus.OPEN) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED,
                    "提案非投票中状态，当前状态: " + entity.getStatus());
        }

        VoteChoice choice = parseChoice(request.choice());

        // 防重复投票
        Optional<VoteEntity> existed = voteRepository.findByProposalIdAndVoterId(proposalId, request.voterId());
        if (existed.isPresent()) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "成员已对该提案投过票");
        }

        VoteEntity vote = new VoteEntity();
        vote.setProposalId(proposalId);
        vote.setVoterId(request.voterId());
        vote.setChoice(choice);
        vote.setVotedAt(LocalDateTime.now());
        voteRepository.save(vote);

        log.info("投票记录成功：proposalId={}, voterId={}", proposalId, request.voterId());
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public VoteResultResponse tallyResult(Long proposalId) {
        ProposalEntity entity = requireProposal(proposalId);

        long yes = voteRepository.countByProposalIdAndChoice(proposalId, VoteChoice.YES);
        long no = voteRepository.countByProposalIdAndChoice(proposalId, VoteChoice.NO);
        long abstain = voteRepository.countByProposalIdAndChoice(proposalId, VoteChoice.ABSTAIN);

        log.info("统计结果：proposalId={}, yes={}, no={}, abstain={}", proposalId, yes, no, abstain);
        return new VoteResultResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus().name(),
                yes,
                no,
                abstain,
                yes + no + abstain
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Proposal getProposal(Long proposalId) {
        return toDomain(requireProposal(proposalId));
    }

    // ===== 内部工具 =====

    private ProposalEntity requireProposal(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "提案不存在: " + id));
    }

    private static ProposalType parseType(String name) {
        try {
            return ProposalType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法提案类型: " + name);
        }
    }

    private static GovernanceChamber parseChamber(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        try {
            return GovernanceChamber.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法议院: " + name);
        }
    }

    private static VoteChoice parseChoice(String name) {
        try {
            return VoteChoice.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法投票选择: " + name);
        }
    }

    private static Proposal toDomain(ProposalEntity entity) {
        return new Proposal(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getType(),
                entity.getStatus(),
                entity.getProposerId(),
                entity.getTargetChamber(),
                entity.getVotingStartAt(),
                entity.getVotingEndAt(),
                entity.getCreatedAt()
        );
    }
}
