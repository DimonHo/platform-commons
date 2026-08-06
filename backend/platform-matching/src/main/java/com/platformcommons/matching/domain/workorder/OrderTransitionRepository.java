package com.platformcommons.matching.domain.workorder;

import com.platformcommons.matching.domain.workorder.OrderTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工单状态流转记录仓储。
 */
@Repository
public interface OrderTransitionRepository extends JpaRepository<OrderTransitionEntity, Long> {

    List<OrderTransitionEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
