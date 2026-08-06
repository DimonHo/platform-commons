package com.platformcommons.matching.domain.location;

import com.platformcommons.matching.domain.location.WorkerLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 劳动者位置 Repository。
 */
public interface WorkerLocationRepository extends JpaRepository<WorkerLocationEntity, String> {

    /**
     * 查询活跃订单数低于阈值的劳动者（用于反榨取过滤）。
     *
     * @param maxActiveOrders 最大活跃订单数
     * @return 劳动者列表
     */
    List<WorkerLocationEntity> findByActiveOrdersLessThan(int maxActiveOrders);
}
