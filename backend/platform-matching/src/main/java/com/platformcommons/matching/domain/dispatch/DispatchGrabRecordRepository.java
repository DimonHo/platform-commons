package com.platformcommons.matching.domain.dispatch;

import com.platformcommons.matching.domain.dispatch.DispatchGrabRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 抢单记录仓储。
 */
@Repository
public interface DispatchGrabRecordRepository extends JpaRepository<DispatchGrabRecordEntity, Long> {

    List<DispatchGrabRecordEntity> findByBroadcastIdOrderByGrabbedAtAsc(Long broadcastId);

    Optional<DispatchGrabRecordEntity> findByBroadcastIdAndWorkerId(Long broadcastId, Long workerId);
}
