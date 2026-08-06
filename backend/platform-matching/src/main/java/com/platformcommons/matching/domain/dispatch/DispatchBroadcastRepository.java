package com.platformcommons.matching.domain.dispatch;

import com.platformcommons.matching.domain.dispatch.BroadcastStatus;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 派单广播仓储。
 */
@Repository
public interface DispatchBroadcastRepository extends JpaRepository<DispatchBroadcastEntity, Long> {

    Optional<DispatchBroadcastEntity> findByBroadcastNo(String broadcastNo);

    List<DispatchBroadcastEntity> findByStatus(BroadcastStatus status);
}
