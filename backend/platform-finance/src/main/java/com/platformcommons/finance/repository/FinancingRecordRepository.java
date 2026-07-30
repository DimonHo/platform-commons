package com.platformcommons.finance.repository;

import com.platformcommons.finance.repository.entity.FinancingRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 融资记录 Repository
 */
public interface FinancingRecordRepository extends JpaRepository<FinancingRecordEntity, Long> {

    /**
     * 根据记录编号查询
     *
     * @param recordId 记录编号
     * @return 融资记录
     */
    Optional<FinancingRecordEntity> findByRecordId(String recordId);
}
