package com.platformcommons.governance.repository;

import com.platformcommons.governance.domain.VoteChoice;
import com.platformcommons.governance.repository.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 投票记录 Repository。
 */
public interface VoteRepository extends JpaRepository<VoteEntity, Long> {

    /**
     * 查询某成员对某提案的投票记录（用于防重复投票）。
     *
     * @param proposalId 提案 ID
     * @param voterId    投票人 ID
     * @return 投票记录
     */
    Optional<VoteEntity> findByProposalIdAndVoterId(Long proposalId, Long voterId);

    /**
     * 统计某提案某选择的票数。
     *
     * @param proposalId 提案 ID
     * @param choice     投票选择
     * @return 票数
     */
    long countByProposalIdAndChoice(Long proposalId, VoteChoice choice);
}
