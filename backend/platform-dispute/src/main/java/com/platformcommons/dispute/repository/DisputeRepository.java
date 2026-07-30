package com.platformcommons.dispute.repository;

import com.platformcommons.dispute.domain.DisputeLevel;
import com.platformcommons.dispute.repository.entity.DisputeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 争议记录 Repository
 */
public interface DisputeRepository extends JpaRepository<DisputeEntity, Long> {

    /**
     * 根据争议编号查询
     *
     * @param disputeId 争议编号
     * @return 争议记录
     */
    Optional<DisputeEntity> findByDisputeId(String disputeId);

    /**
     * 根据申诉人查询
     *
     * @param filedBy 申诉人
     * @return 争议记录列表
     */
    List<DisputeEntity> findByFiledBy(String filedBy);

    /**
     * 根据救济层级查询
     *
     * @param level 救济层级
     * @return 争议记录列表
     */
    List<DisputeEntity> findByLevel(DisputeLevel level);
}
