package com.platformcommons.dispute.service.impl;

import com.platformcommons.dispute.domain.Dispute;
import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.domain.DisputeStatus;
import com.platformcommons.common.util.SnowflakeIdGenerator;
import com.platformcommons.dispute.repository.DisputeRepository;
import com.platformcommons.dispute.repository.entity.DisputeEntity;
import com.platformcommons.dispute.service.DisputeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 争议申诉服务实现（第15章 第93-96条）
 * <p>
 * 三级递进救济流程：
 * <ol>
 *   <li>业务团队复核 → 不满意可上诉</li>
 *   <li>申诉委员会审议 → 不满意可上诉</li>
 *   <li>外部调解/仲裁（最终级）</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {


    private final DisputeRepository disputeRepository;


    @Override
    @Transactional
    public String fileDispute(String filedBy, String subject, String description) {
        String disputeId = SnowflakeIdGenerator.nextId();
        log.info("提交争议申诉: disputeId={}, filedBy={}, subject={}", disputeId, filedBy, subject);

        DisputeEntity entity = new DisputeEntity();
        entity.setDisputeId(disputeId);
        entity.setFiledBy(filedBy);
        entity.setSubject(subject);
        entity.setDescription(description);
        entity.setLevel(DisputeLevel.BUSINESS_REVIEW);
        entity.setStatus(DisputeStatus.FILED);
        entity.setFiledAt(Instant.now().toString());
        disputeRepository.save(entity);

        return disputeId;
    }

    @Override
    @Transactional
    public Dispute resolveDispute(String disputeId, String resolution) {
        log.info("裁决争议: disputeId={}, resolution={}", disputeId, resolution);

        DisputeEntity entity = disputeRepository.findByDisputeId(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("争议记录不存在: " + disputeId));

        entity.setStatus(DisputeStatus.RESOLVED);
        entity.setResolution(resolution);
        disputeRepository.save(entity);

        return toDomain(entity);
    }

    @Override
    @Transactional
    public Dispute appeal(String disputeId) {
        log.info("争议上诉: disputeId={}", disputeId);

        DisputeEntity entity = disputeRepository.findByDisputeId(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("争议记录不存在: " + disputeId));

        DisputeLevel nextLevel = switch (entity.getLevel()) {
            case BUSINESS_REVIEW -> DisputeLevel.APPEAL_COMMITTEE;
            case APPEAL_COMMITTEE -> DisputeLevel.EXTERNAL;
            case EXTERNAL -> throw new IllegalStateException("已处于最高救济层级，无法继续上诉: " + disputeId);
        };

        entity.setLevel(nextLevel);
        entity.setStatus(DisputeStatus.APPEALED);
        disputeRepository.save(entity);

        log.info("争议升级至: disputeId={}, newLevel={}", disputeId, nextLevel.getDescription());
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> getDispute(String disputeId) {
        return disputeRepository.findByDisputeId(disputeId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> listDisputesByUser(String filedBy) {
        return disputeRepository.findByFiledBy(filedBy).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> listAllDisputes() {
        return disputeRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> listDisputesByLevel(DisputeLevel level) {
        return disputeRepository.findByLevel(level).stream()
                .map(this::toDomain)
                .toList();
    }

    private Dispute toDomain(DisputeEntity entity) {
        return new Dispute(
                entity.getDisputeId(),
                entity.getFiledBy(),
                entity.getSubject(),
                entity.getDescription(),
                entity.getLevel(),
                entity.getStatus(),
                entity.getResolution(),
                entity.getFiledAt()
        );
    }
}
