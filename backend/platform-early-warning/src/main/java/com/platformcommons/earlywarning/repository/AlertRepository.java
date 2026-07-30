package com.platformcommons.earlywarning.repository;

import com.platformcommons.earlywarning.repository.entity.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * 预警 Repository。
 */
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {

    /**
     * 查询所有未解除的预警（acknowledged=false），按触发时间降序。
     *
     * @return 预警列表
     */
    List<AlertEntity> findByAcknowledgedFalseOrderByTriggeredAtDesc();
}
