package com.platformcommons.matching.domain.workorder;

import com.platformcommons.matching.domain.workorder.WorkOrderStatus;
import com.platformcommons.matching.domain.workorder.WorkOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 业务工单仓储。
 */
@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, Long> {

    Optional<WorkOrderEntity> findByOrderNo(String orderNo);

    List<WorkOrderEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<WorkOrderEntity> findByWorkerIdOrderByCreatedAtDesc(Long workerId);

    List<WorkOrderEntity> findByStatus(WorkOrderStatus status);
}
