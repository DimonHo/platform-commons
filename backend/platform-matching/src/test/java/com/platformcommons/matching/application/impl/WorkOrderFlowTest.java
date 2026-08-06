package com.platformcommons.matching.application.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.matching.domain.dispatch.BroadcastStatus;
import com.platformcommons.matching.domain.dispatch.BroadcastType;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcastEntity;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcastRepository;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecordEntity;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecordRepository;
import com.platformcommons.matching.domain.dispatch.GrabStatus;
import com.platformcommons.matching.domain.location.WorkerLocationEntity;
import com.platformcommons.matching.domain.location.WorkerLocationRepository;
import com.platformcommons.matching.domain.match.MatchResult;
import com.platformcommons.matching.domain.workorder.OperatorRole;
import com.platformcommons.matching.domain.workorder.OrderPriority;
import com.platformcommons.matching.domain.workorder.OrderTransitionEntity;
import com.platformcommons.matching.domain.workorder.OrderTransitionRepository;
import com.platformcommons.matching.domain.workorder.TransitionAction;
import com.platformcommons.matching.domain.workorder.WorkOrder;
import com.platformcommons.matching.domain.workorder.WorkOrderEntity;
import com.platformcommons.matching.domain.workorder.WorkOrderRepository;
import com.platformcommons.matching.domain.workorder.WorkOrderStatus;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 工单全流程单元测试（纯 Mockito，无 Spring 上下文、无数据库）。
 *
 * <p>覆盖四类场景：</p>
 * <ul>
 *   <li>① 工单状态机：合法全流程流转 + 非法迁移抛业务异常</li>
 *   <li>② 流转日志：每次状态变更必须落一条 {@link OrderTransitionEntity}</li>
 *   <li>③ 匹配引擎 JPA 化：候选劳动者从仓储读取，不再依赖内存态</li>
 *   <li>④ 抢单并发一致性：assignWorker 乐观锁冲突异常不再被吞噬，整体回滚无重复 WIN</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderFlowTest {

    @Mock
    private WorkOrderRepository workOrderRepository;
    @Mock
    private OrderTransitionRepository orderTransitionRepository;
    @Mock
    private WorkerLocationRepository workerLocationRepository;
    @Mock
    private DispatchBroadcastRepository broadcastRepository;
    @Mock
    private DispatchGrabRecordRepository grabRecordRepository;

    private WorkOrderServiceImpl workOrderService;
    private MatchingEngineServiceImpl matchingEngineService;
    private DispatchServiceImpl dispatchService;

    @BeforeEach
    void setUp() {
        workOrderService = new WorkOrderServiceImpl(workOrderRepository, orderTransitionRepository);
        matchingEngineService = new MatchingEngineServiceImpl(workerLocationRepository);
        dispatchService = new DispatchServiceImpl(broadcastRepository, grabRecordRepository, workOrderService);
    }

    // ============ ① 状态机 ============

    @Test
    @DisplayName("合法全流程：创建→派单→接单→开始→提交→验收→结算，每次流转落一条日志")
    void fullLifecycleTransition_shouldSucceedAndLogEachTransition() {
        when(workOrderRepository.save(any(WorkOrderEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkOrder created = workOrderService.createOrder(1001L, WorkOrderType.SERVICE, "保洁服务",
                "打扫 120㎡ 办公室", new BigDecimal("299.00"), 31.2304, 121.4737, null, OrderPriority.NORMAL);
        assertEquals(WorkOrderStatus.CREATED, created.status());

        // 捕获 createOrder 落库实体并回填主键，供后续流转查询
        ArgumentCaptor<WorkOrderEntity> saveCaptor = ArgumentCaptor.forClass(WorkOrderEntity.class);
        verify(workOrderRepository, times(1)).save(saveCaptor.capture());
        WorkOrderEntity entity = saveCaptor.getValue();
        assertEquals(WorkOrderStatus.CREATED, entity.getStatus());
        entity.setId(1L);
        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        WorkOrder dispatched = workOrderService.transitionOrder(1L, TransitionAction.DISPATCH,
                2001L, OperatorRole.SYSTEM, "系统派单", null);
        assertEquals(WorkOrderStatus.DISPATCHED, dispatched.status());

        WorkOrder accepted = workOrderService.transitionOrder(1L, TransitionAction.ACCEPT,
                3001L, OperatorRole.WORKER, "劳动者接单", null);
        assertEquals(WorkOrderStatus.ACCEPTED, accepted.status());

        WorkOrder started = workOrderService.transitionOrder(1L, TransitionAction.START,
                3001L, OperatorRole.WORKER, "开始服务", null);
        assertEquals(WorkOrderStatus.IN_PROGRESS, started.status());

        WorkOrder submitted = workOrderService.transitionOrder(1L, TransitionAction.SUBMIT,
                3001L, OperatorRole.WORKER, "提交验收", null);
        assertEquals(WorkOrderStatus.SUBMITTED, submitted.status());

        WorkOrder approved = workOrderService.transitionOrder(1L, TransitionAction.APPROVE,
                1001L, OperatorRole.MEMBER, "验收通过", null);
        assertEquals(WorkOrderStatus.APPROVED, approved.status());

        WorkOrder settled = workOrderService.transitionOrder(1L, TransitionAction.SETTLE,
                2001L, OperatorRole.SYSTEM, "结算完成", null);
        assertEquals(WorkOrderStatus.SETTLED, settled.status());

        // 内存实体状态同步推进
        assertEquals(WorkOrderStatus.SETTLED, entity.getStatus());
        // 1 次创建 + 6 次流转共落库 7 次
        verify(workOrderRepository, times(7)).save(any(WorkOrderEntity.class));

        // ② 六次流转共落六条流转日志，首尾动作与状态正确
        ArgumentCaptor<OrderTransitionEntity> logCaptor = ArgumentCaptor.forClass(OrderTransitionEntity.class);
        verify(orderTransitionRepository, times(6)).save(logCaptor.capture());
        List<OrderTransitionEntity> logs = logCaptor.getAllValues();
        assertEquals(TransitionAction.DISPATCH, logs.get(0).getAction());
        assertEquals(WorkOrderStatus.CREATED, logs.get(0).getFromStatus());
        assertEquals(WorkOrderStatus.DISPATCHED, logs.get(0).getToStatus());
        assertEquals(TransitionAction.SETTLE, logs.get(5).getAction());
        assertEquals(WorkOrderStatus.APPROVED, logs.get(5).getFromStatus());
        assertEquals(WorkOrderStatus.SETTLED, logs.get(5).getToStatus());
    }

    @Test
    @DisplayName("非法迁移：CREATED 直接 ACCEPT 抛业务异常，状态不变且不落日志")
    void illegalTransition_shouldThrowBusinessException() {
        WorkOrderEntity entity = newEntity(1L, WorkOrderStatus.CREATED);
        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(entity));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> workOrderService.transitionOrder(1L, TransitionAction.ACCEPT,
                        3001L, OperatorRole.WORKER, null, null));
        assertTrue(ex.getMessage().contains("非法状态流转"));
        // 状态未被污染，也不产生流转日志
        assertEquals(WorkOrderStatus.CREATED, entity.getStatus());
        verify(orderTransitionRepository, never()).save(any(OrderTransitionEntity.class));
    }

    // ============ ③ 匹配引擎 JPA 化 ============

    @Test
    @DisplayName("匹配引擎：registerWorker 落库持久化")
    void registerWorker_shouldPersistToRepository() {
        matchingEngineService.registerWorker("w1", 31.2304, 121.4737, 1, 4.9, 30);

        ArgumentCaptor<WorkerLocationEntity> captor = ArgumentCaptor.forClass(WorkerLocationEntity.class);
        verify(workerLocationRepository).save(captor.capture());
        WorkerLocationEntity saved = captor.getValue();
        assertEquals("w1", saved.getWorkerId());
        assertEquals(31.2304, saved.getLatitude());
        assertEquals(121.4737, saved.getLongitude());
        assertEquals(1, saved.getActiveOrders());
        assertEquals(4.9, saved.getRating());
        assertEquals(30, saved.getRegistrationDays());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("匹配引擎：match 从 JPA 仓储读取候选（反榨取预过滤），不再读内存态")
    void match_shouldReadCandidatesFromRepository() {
        when(workerLocationRepository.findByActiveOrdersLessThan(3))
                .thenReturn(List.of(newWorker("w1", 1, 4.9, 30)));

        MatchResult result = matchingEngineService.match("task-1", "NEAREST_FIRST");

        assertEquals("NEAREST_FIRST", result.strategyName());
        assertFalse(result.matchedWorkers().isEmpty());
        assertEquals("w1", result.matchedWorkers().get(0));
        // 关键断言：候选来自仓储查询（旧内存态实现不会触发该查询，匹配结果为空）
        verify(workerLocationRepository).findByActiveOrdersLessThan(3);
    }

    @Test
    @DisplayName("匹配引擎：listWorkers 返回仓储数据，而非内存态")
    void listWorkers_shouldReturnRepositoryData() {
        when(workerLocationRepository.findAll()).thenReturn(List.of(
                newWorker("w1", 1, 4.9, 30),
                newWorker("w2", 0, 4.5, 60)));

        assertEquals(List.of("w1", "w2"), matchingEngineService.listWorkers());
        verify(workerLocationRepository).findAll();
    }

    // ============ ④ 抢单并发一致性 ============

    @Test
    @DisplayName("抢单冲突：assignWorker 乐观锁异常不再被吞噬，事务回滚且无重复 WIN")
    void grabOrder_optimisticLockConflict_shouldPropagateAndKeepConsistency() {
        // 广播：第一次调用未达标；第二次调用已达标（模拟并发下另一事务已先落库）
        DispatchBroadcastEntity broadcastV1 = newBroadcast(0, BroadcastStatus.BROADCASTING);
        DispatchBroadcastEntity broadcastV2 = newBroadcast(1, BroadcastStatus.BROADCASTING);
        when(broadcastRepository.findById(1L))
                .thenReturn(Optional.of(broadcastV1), Optional.of(broadcastV2));

        // 抢单记录：第一次调用只有 A；第二次调用 A 已 WIN、B 仍 PENDING
        DispatchGrabRecordEntity recA = newGrabRecord(1L, 1001L, GrabStatus.WIN,
                Instant.now().minusSeconds(10));
        DispatchGrabRecordEntity recB = newGrabRecord(2L, 1002L, GrabStatus.PENDING, Instant.now());
        when(grabRecordRepository.findByBroadcastIdOrderByGrabbedAtAsc(1L))
                .thenReturn(List.of(recA), List.of(recA, recB));
        when(grabRecordRepository.save(any(DispatchGrabRecordEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // 工单：第一次指派成功；第二次指派时 work_order 版本冲突（乐观锁）
        WorkOrderEntity order = newEntity(1L, WorkOrderStatus.DISPATCHED);
        when(workOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(workOrderRepository.save(any(WorkOrderEntity.class)))
                .thenReturn(order)
                .thenThrow(new ObjectOptimisticLockingFailureException(WorkOrderEntity.class, 1L));

        // 第一次抢单：正常胜出并指派
        assertNotNull(dispatchService.grabOrder(1L, 1001L, 31.2304, 121.4737));
        assertEquals(1001L, order.getWorkerId());

        // 第二次抢单：乐观锁冲突必须抛出（原异常），而不是被吞掉后继续提交
        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> dispatchService.grabOrder(1L, 1002L, 31.2305, 121.4738));

        // 一致性断言：第二次抢单的胜负判定未落库（saveAll 仅第一次调用一次）
        verify(grabRecordRepository, times(1)).saveAll(anyList());
        // B 始终为 PENDING，未被重复标记 WIN（也无 LOSE）——无重复 WIN
        assertEquals(GrabStatus.PENDING, recB.getStatus());
        assertEquals(GrabStatus.WIN, recA.getStatus());
        // 广播保存仅第一次发生（第二次整体回滚，grabbed_count 不丢失更新）
        verify(broadcastRepository, times(1)).save(any(DispatchBroadcastEntity.class));
        // 工单指派共尝试两次：第一次成功落库，第二次在 save 时触发乐观锁冲突上抛回滚
        verify(workOrderRepository, times(2)).save(any(WorkOrderEntity.class));
    }

    // ============ 测试辅助 ============

    private static WorkOrderEntity newEntity(Long id, WorkOrderStatus status) {
        WorkOrderEntity e = new WorkOrderEntity();
        e.setId(id);
        e.setOrderNo("ORD" + id);
        e.setOrderType(WorkOrderType.SERVICE);
        e.setTitle("测试工单");
        e.setMemberId(1001L);
        e.setAmount(new BigDecimal("100.00"));
        e.setStatus(status);
        e.setPriority(OrderPriority.NORMAL);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private static WorkerLocationEntity newWorker(String workerId, int activeOrders,
                                                  double rating, int registrationDays) {
        WorkerLocationEntity e = new WorkerLocationEntity();
        e.setWorkerId(workerId);
        e.setLatitude(31.2304);
        e.setLongitude(121.4737);
        e.setActiveOrders(activeOrders);
        e.setRating(rating);
        e.setRegistrationDays(registrationDays);
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private static DispatchBroadcastEntity newBroadcast(int grabbedCount, BroadcastStatus status) {
        DispatchBroadcastEntity e = new DispatchBroadcastEntity();
        e.setId(1L);
        e.setBroadcastNo("BCAST001");
        e.setOrderId(1L);
        e.setOrderType(WorkOrderType.SERVICE);
        e.setBroadcastType(BroadcastType.GRAB);
        e.setTargetCount(1);
        e.setGrabbedCount(grabbedCount);
        e.setStatus(status);
        e.setExpireAt(Instant.now().plus(Duration.ofMinutes(5)));
        e.setCreatedAt(Instant.now());
        return e;
    }

    private static DispatchGrabRecordEntity newGrabRecord(Long id, Long workerId,
                                                          GrabStatus status, Instant grabbedAt) {
        DispatchGrabRecordEntity e = new DispatchGrabRecordEntity();
        e.setId(id);
        e.setBroadcastId(1L);
        e.setWorkerId(workerId);
        e.setStatus(status);
        e.setGrabbedAt(grabbedAt);
        return e;
    }
}
