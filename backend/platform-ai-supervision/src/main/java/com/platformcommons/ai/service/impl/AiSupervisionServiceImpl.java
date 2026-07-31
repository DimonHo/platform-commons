package com.platformcommons.ai.service.impl;

import com.platformcommons.ai.domain.AiReviewRole;
import com.platformcommons.ai.domain.MandatoryReviewItem;
import com.platformcommons.ai.domain.ReviewResult;
import com.platformcommons.ai.domain.ReviewStatus;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.ai.repository.AiReviewRecordRepository;
import com.platformcommons.ai.repository.entity.AiReviewRecordEntity;
import com.platformcommons.ai.service.AiSupervisionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * AI 公共监督审议服务实现（第12章 第60-69条）
 * <p>
 * 多角色交叉审议编排：依次调用七类审议角色，
 * 汇总各方意见形成审议结论。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSupervisionServiceImpl implements AiSupervisionService {


    private final AiReviewRecordRepository reviewRecordRepository;


    @Override
    @Transactional
    public String initiateReview(MandatoryReviewItem item, String proposal) {
        String reviewId = generateReviewId();
        log.info("发起强制审议: reviewId={}, item={}, proposal={}", reviewId, item.getDescription(), proposal);

        AiReviewRecordEntity entity = new AiReviewRecordEntity();
        entity.setReviewId(reviewId);
        entity.setMandatoryItem(item);
        entity.setProposal(proposal);
        entity.setStatus(ReviewStatus.PENDING);
        reviewRecordRepository.save(entity);

        return reviewId;
    }

    @Override
    @Transactional
    public ReviewResult conductReview(String reviewId) {
        log.info("执行多角色交叉审议: reviewId={}", reviewId);

        AiReviewRecordEntity entity = reviewRecordRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("审议记录不存在: " + reviewId));

        entity.setStatus(ReviewStatus.IN_PROGRESS);
        reviewRecordRepository.save(entity);

        Set<String> beneficiaries = new LinkedHashSet<>();
        Set<String> costBearers = new LinkedHashSet<>();
        Set<String> dissentingViews = new LinkedHashSet<>();
        boolean consensusReached = true;

        for (AiReviewRole role : AiReviewRole.values()) {
            String opinion = simulateRoleOpinion(role, entity.getProposal());
            if (opinion.startsWith("受益")) {
                beneficiaries.add(opinion);
            } else if (opinion.startsWith("成本")) {
                costBearers.add(opinion);
            } else if (opinion.startsWith("反对")) {
                dissentingViews.add(role.getDescription() + ": " + opinion);
                consensusReached = false;
            }
        }

        entity.setStatus(consensusReached ? ReviewStatus.COMPLETED : ReviewStatus.CONTESTED);
        entity.setConsensusReached(consensusReached);
        reviewRecordRepository.save(entity);

        ReviewResult result = new ReviewResult(
                reviewId,
                beneficiaries,
                costBearers,
                consensusReached ? null : "建议分阶段实施并设置观察期",
                dissentingViews,
                consensusReached,
                buildSummary(entity.getMandatoryItem(), consensusReached, dissentingViews.size())
        );
        log.info("审议完成: reviewId={}, consensus={}", reviewId, consensusReached);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewStatus> getReviewStatus(String reviewId) {
        return reviewRecordRepository.findByReviewId(reviewId)
                .map(AiReviewRecordEntity::getStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResult> listAllReviews() {
        return reviewRecordRepository.findAll().stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    @Transactional
    public void contestReview(String reviewId, String dissent) {
        log.info("提交审议争议: reviewId={}, dissent={}", reviewId, dissent);
        AiReviewRecordEntity entity = reviewRecordRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("审议记录不存在: " + reviewId));
        entity.setStatus(ReviewStatus.CONTESTED);
        entity.setDissent(dissent);
        reviewRecordRepository.save(entity);
    }

    private String generateReviewId() {
        return SnowflakeUtils.nextId();
    }

    /**
     * 模拟各审议角色的意见（实际实现对接 AI 模型）
     */
    private String simulateRoleOpinion(AiReviewRole role, String proposal) {
        return switch (role) {
            case PUBLIC_INTEREST -> "受益: 全体成员公共福利";
            case WORKER_RIGHTS -> "成本: 劳动者劳动强度可能增加";
            case CONSUMER_RIGHTS -> "受益: 消费者获得更优价格";
            case MINORITY_PROTECTION -> "反对: 可能对弱势劳动者不利";
            case CAPITAL_AUDIT -> "受益: 平台运营效率提升";
            case COUNTER_ARGUMENT -> "反对: 长期依赖算法可能导致治理空心化";
            case FACT_CHECK -> "受益: 决策过程可追溯审计";
        };
    }

    private String buildSummary(MandatoryReviewItem item, boolean consensus, int dissentCount) {
        if (consensus) {
            return "审议通过: " + item.getDescription() + "（无分歧）";
        }
        return "审议存在分歧: " + item.getDescription() + "（" + dissentCount + " 项反对意见）";
    }

    private ReviewResult toResult(AiReviewRecordEntity entity) {
        return new ReviewResult(
                entity.getReviewId(),
                Set.of(),
                Set.of(),
                null,
                Set.of(),
                Boolean.TRUE.equals(entity.getConsensusReached()),
                entity.getProposal()
        );
    }
}
