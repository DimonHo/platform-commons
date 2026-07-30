package com.platformcommons.governance.repository;

import com.platformcommons.governance.repository.entity.ProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 提案 Repository。
 */
public interface ProposalRepository extends JpaRepository<ProposalEntity, Long> {
}
