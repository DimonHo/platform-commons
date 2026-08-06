package com.platformcommons.matching.application.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.matching.domain.dispatch.BroadcastStatus;
import com.platformcommons.matching.domain.dispatch.BroadcastType;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcast;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecord;
import com.platformcommons.matching.domain.dispatch.GrabStatus;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcastRepository;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecordRepository;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcastEntity;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecordEntity;
import com.platformcommons.matching.application.DispatchService;
import com.platformcommons.matching.application.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 派单广播服务实现。
 *
 * <p>核心逻辑：{@link #grabOrder} 实现抢单——校验广播有效性 → 幂等检查 →
 * 落库抢单记录 → 抢单人数达标后关闭广播并为胜出者指派工单。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DispatchServiceImpl implements DispatchService {

    /** 默认广播有效期（5 分钟）。 */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    /** 默认目标抢单人数。 */
    private static final int DEFAULT_TARGET_COUNT = 1;

    private final DispatchBroadcastRepository broadcastRepository;
    private final DispatchGrabRecordRepository grabRecordRepository;
    private final WorkOrderService workOrderService;

    @Override
    public DispatchBroadcast createBroadcast(Long orderId, WorkOrderType orderType, Double centerLat,
                                             Double centerLng, Integer radiusMeters, Integer targetCount,
                                             BroadcastType broadcastType) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(orderType, "orderType must not be null");
        Objects.requireNonNull(broadcastType, "broadcastType must not be null");

        Instant now = Instant.now();
        DispatchBroadcastEntity entity = new DispatchBroadcastEntity();
        entity.setBroadcastNo(SnowflakeUtils.nextId("BCST"));
        entity.setOrderId(orderId);
        entity.setOrderType(orderType);
        entity.setBroadcastType(broadcastType);
        entity.setCenterLat(centerLat);
        entity.setCenterLng(centerLng);
        entity.setRadiusMeters(radiusMeters);
        entity.setTargetCount(targetCount != null && targetCount > 0 ? targetCount : DEFAULT_TARGET_COUNT);
        entity.setGrabbedCount(0);
        entity.setStatus(BroadcastStatus.BROADCASTING);
        entity.setExpireAt(now.plus(DEFAULT_TTL));
        entity.setCreatedAt(now);

        DispatchBroadcastEntity saved = broadcastRepository.save(entity);
        log.info("Dispatch broadcast created: broadcastNo={}, orderId={}, type={}",
                saved.getBroadcastNo(), orderId, broadcastType);
        return toDomain(saved);
    }

    @Override
    public DispatchGrabRecord grabOrder(Long broadcastId, Long workerId, Double workerLat, Double workerLng) {
        Objects.requireNonNull(broadcastId, "broadcastId must not be null");
        Objects.requireNonNull(workerId, "workerId must not be null");

        DispatchBroadcastEntity broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new BusinessException("广播不存在: id=" + broadcastId));

        // 1. 广播必须处于广播中且未过期
        if (broadcast.getStatus() != BroadcastStatus.BROADCASTING) {
            throw new BusinessException("广播不在广播中，当前状态: " + broadcast.getStatus().getDescription());
        }
        if (broadcast.getExpireAt() != null && Instant.now().isAfter(broadcast.getExpireAt())) {
            broadcast.setStatus(BroadcastStatus.EXPIRED);
            broadcastRepository.save(broadcast);
            throw new BusinessException("广播已过期: " + broadcast.getBroadcastNo());
        }

        // 2. 幂等：同一劳动者不能重复抢单
        if (grabRecordRepository.findByBroadcastIdAndWorkerId(broadcastId, workerId).isPresent()) {
            throw new BusinessException("劳动者已抢过该广播: workerId=" + workerId);
        }

        // 3. 落库抢单记录（先标记 PENDING）
        int distance = computeDistanceMeters(broadcast.getCenterLat(), broadcast.getCenterLng(),
                workerLat, workerLng);
        Instant now = Instant.now();
        DispatchGrabRecordEntity grab = new DispatchGrabRecordEntity();
        grab.setBroadcastId(broadcastId);
        grab.setWorkerId(workerId);
        grab.setWorkerLat(workerLat);
        grab.setWorkerLng(workerLng);
        grab.setDistanceMeters(distance);
        grab.setStatus(GrabStatus.PENDING);
        grab.setGrabbedAt(now);
        grab = grabRecordRepository.save(grab);

        // 4. 累计抢单人数；达标后关闭广播并判定胜负
        int grabbedCount = nullSafeInt(broadcast.getGrabbedCount()) + 1;
        broadcast.setGrabbedCount(grabbedCount);

        int target = nullSafeInt(broadcast.getTargetCount());
        if (grabbedCount >= target) {
            broadcast.setStatus(BroadcastStatus.GRABBED);
            resolveGrabResults(broadcastId, target);
        }
        broadcastRepository.save(broadcast);

        // 重新读取：广播关闭后 grab 状态可能已被更新
        grab = grabRecordRepository.findById(grab.getId()).orElse(grab);
        log.info("Grab result: broadcastId={}, workerId={}, status={}, distance={}m",
                broadcastId, workerId, grab.getStatus(), distance);
        return toDomain(grab);
    }

    @Override
    @Transactional(readOnly = true)
    public DispatchBroadcast getBroadcast(String broadcastNo) {
        Objects.requireNonNull(broadcastNo, "broadcastNo must not be null");
        return broadcastRepository.findByBroadcastNo(broadcastNo)
                .map(DispatchServiceImpl::toDomain)
                .orElseThrow(() -> new BusinessException("广播不存在: " + broadcastNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchBroadcast> listActiveBroadcasts() {
        return broadcastRepository.findByStatus(BroadcastStatus.BROADCASTING).stream()
                .map(DispatchServiceImpl::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispatchGrabRecord> listGrabRecords(Long broadcastId) {
        Objects.requireNonNull(broadcastId, "broadcastId must not be null");
        return grabRecordRepository.findByBroadcastIdOrderByGrabbedAtAsc(broadcastId).stream()
                .map(DispatchServiceImpl::toDomain)
                .toList();
    }

    /**
     * 广播关闭后判定抢单胜负：
     * <ul>
     *   <li>按抢单时间升序取前 {@code target} 名为 WIN（先到先得）</li>
     *   <li>其余标记为 LOSE</li>
     * </ul>
     * 同时为每位胜出者指派工单（仅首位胜出者实际绑定到工单，
     * 多人场景下后续胜出者由业务层另行处理）。
     */
    private void resolveGrabResults(Long broadcastId, int target) {
        List<DispatchGrabRecordEntity> records =
                grabRecordRepository.findByBroadcastIdOrderByGrabbedAtAsc(broadcastId);
        boolean firstWinnerAssigned = false;
        // 需要广播的 orderId 用于指派
        DispatchBroadcastEntity broadcast = broadcastRepository.findById(broadcastId).orElse(null);
        for (int i = 0; i < records.size(); i++) {
            DispatchGrabRecordEntity r = records.get(i);
            if (i < target) {
                r.setStatus(GrabStatus.WIN);
                // 首位胜出者绑定工单；指派失败（如 work_order 乐观锁冲突）必须上抛，
                // 使整个 grabOrder 事务回滚，避免 grabbed_count 与 WIN 判定不一致
                if (!firstWinnerAssigned && broadcast != null) {
                    workOrderService.assignWorker(broadcast.getOrderId(), r.getWorkerId());
                    firstWinnerAssigned = true;
                }
            } else {
                r.setStatus(GrabStatus.LOSE);
            }
        }
        grabRecordRepository.saveAll(records);
    }

    /**
     * 计算距离（米）。优先使用 Haversine，坐标缺失时返回 0。
     */
    private static int computeDistanceMeters(Double centerLat, Double centerLng,
                                             Double workerLat, Double workerLng) {
        if (centerLat == null || centerLng == null || workerLat == null || workerLng == null) {
            return 0;
        }
        double dx = workerLat - centerLat;
        double dy = workerLng - centerLng;
        // 简化估算：1° ≈ 111km，演示场景足够
        double km = Math.sqrt(dx * dx + dy * dy) * 111.0;
        return (int) Math.round(km * 1000);
    }

    /** 空安全整数读取，null 视作 0。 */
    private static int nullSafeInt(Integer value) {
        return value != null ? value : 0;
    }

    private static DispatchBroadcast toDomain(DispatchBroadcastEntity e) {
        return new DispatchBroadcast(
                e.getId(), e.getBroadcastNo(), e.getOrderId(), e.getOrderType(), e.getBroadcastType(),
                e.getCenterLat(), e.getCenterLng(), e.getRadiusMeters(), e.getTargetCount(),
                e.getGrabbedCount(), e.getStatus(), e.getExpireAt(), e.getCreatedAt()
        );
    }

    private static DispatchGrabRecord toDomain(DispatchGrabRecordEntity e) {
        return new DispatchGrabRecord(
                e.getId(), e.getBroadcastId(), e.getWorkerId(), e.getWorkerLat(), e.getWorkerLng(),
                e.getDistanceMeters(), e.getStatus(), e.getGrabbedAt()
        );
    }
}
