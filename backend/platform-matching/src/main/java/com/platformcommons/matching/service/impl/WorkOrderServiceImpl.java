package com.platformcommons.matching.service.impl;

import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.matching.domain.OrderPriority;
import com.platformcommons.matching.domain.OrderTransition;
import com.platformcommons.matching.domain.OperatorRole;
import com.platformcommons.matching.domain.TransitionAction;
import com.platformcommons.matching.domain.WorkOrder;
import com.platformcommons.matching.domain.WorkOrderStatus;
import com.platformcommons.matching.domain.WorkOrderType;
import com.platformcommons.matching.repository.OrderTransitionRepository;
import com.platformcommons.matching.repository.WorkOrderRepository;
import com.platformcommons.matching.repository.entity.OrderTransitionEntity;
import com.platformcommons.matching.repository.entity.WorkOrderEntity;
import com.platformcommons.matching.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 业务工单服务实现。
 *
 * <p>核心逻辑：状态机校验（{@link #transitionOrder}）驱动工单流转，
 * 每次状态变更均落库 {@link OrderTransitionEntity} 留痕。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final OrderTransitionRepository orderTransitionRepository;

    /** 每个 {@link TransitionAction} 允许的前置状态集合。 */
    private static final Map<TransitionAction, Set<WorkOrderStatus>> TRANSITION_RULES = Map.of(
            TransitionAction.DISPATCH, EnumSet.of(WorkOrderStatus.CREATED),
            TransitionAction.ACCEPT, EnumSet.of(WorkOrderStatus.DISPATCHED),
            TransitionAction.START, EnumSet.of(WorkOrderStatus.ACCEPTED),
            TransitionAction.SUBMIT, EnumSet.of(WorkOrderStatus.IN_PROGRESS),
            TransitionAction.APPROVE, EnumSet.of(WorkOrderStatus.SUBMITTED),
            TransitionAction.REJECT, EnumSet.of(WorkOrderStatus.SUBMITTED),
            // 允许在多个活跃态下取消
            TransitionAction.CANCEL, EnumSet.of(WorkOrderStatus.CREATED, WorkOrderStatus.ACCEPTED,
                    WorkOrderStatus.DISPATCHED),
            // 争议可从任何"进行中"类状态发起
            TransitionAction.DISPUTE, EnumSet.of(
                    WorkOrderStatus.ACCEPTED, WorkOrderStatus.IN_PROGRESS,
                    WorkOrderStatus.SUBMITTED, WorkOrderStatus.APPROVED, WorkOrderStatus.REJECTED),
            TransitionAction.SETTLE, EnumSet.of(WorkOrderStatus.APPROVED)
    );

    /** 动作到目标状态的映射。 */
    private static final Map<TransitionAction, WorkOrderStatus> ACTION_TARGET = Map.of(
            TransitionAction.DISPATCH, WorkOrderStatus.DISPATCHED,
            TransitionAction.ACCEPT, WorkOrderStatus.ACCEPTED,
            TransitionAction.START, WorkOrderStatus.IN_PROGRESS,
            TransitionAction.SUBMIT, WorkOrderStatus.SUBMITTED,
            TransitionAction.APPROVE, WorkOrderStatus.APPROVED,
            // 驳回后回到进行中，劳动者重新执行
            TransitionAction.REJECT, WorkOrderStatus.IN_PROGRESS,
            TransitionAction.CANCEL, WorkOrderStatus.CANCELLED,
            TransitionAction.DISPUTE, WorkOrderStatus.DISPUTED,
            TransitionAction.SETTLE, WorkOrderStatus.SETTLED
    );

    @Override
    public WorkOrder createOrder(Long memberId, WorkOrderType orderType, String title, String description,
                                 BigDecimal amount, Double lat, Double lng, Instant scheduledAt,
                                 OrderPriority priority) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        Objects.requireNonNull(orderType, "orderType must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(amount, "amount must not be null");

        Instant now = Instant.now();
        WorkOrderEntity entity = new WorkOrderEntity();
        entity.setOrderNo(SnowflakeUtils.nextId("ORD"));
        entity.setOrderType(orderType);
        entity.setTitle(title);
        entity.setDescription(description);
        entity.setMemberId(memberId);
        entity.setAmount(amount);
        entity.setStatus(WorkOrderStatus.CREATED);
        entity.setPriority(priority != null ? priority : OrderPriority.NORMAL);
        entity.setLocationLat(lat);
        entity.setLocationLng(lng);
        entity.setScheduledAt(scheduledAt);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        WorkOrderEntity saved = workOrderRepository.save(entity);
        log.info("Work order created: orderNo={}, memberId={}, type={}", saved.getOrderNo(), memberId, orderType);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrder getOrder(String orderNo) {
        Objects.requireNonNull(orderNo, "orderNo must not be null");
        return workOrderRepository.findByOrderNo(orderNo)
                .map(WorkOrderServiceImpl::toDomain)
                .orElseThrow(() -> new BusinessException("工单不存在: " + orderNo));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrder getOrder(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return workOrderRepository.findById(orderId)
                .map(WorkOrderServiceImpl::toDomain)
                .orElseThrow(() -> new BusinessException("工单不存在: id=" + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrder> listMemberOrders(Long memberId) {
        Objects.requireNonNull(memberId, "memberId must not be null");
        return workOrderRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(WorkOrderServiceImpl::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrder> listWorkerOrders(Long workerId) {
        Objects.requireNonNull(workerId, "workerId must not be null");
        return workOrderRepository.findByWorkerIdOrderByCreatedAtDesc(workerId).stream()
                .map(WorkOrderServiceImpl::toDomain)
                .toList();
    }

    @Override
    public WorkOrder transitionOrder(Long orderId, TransitionAction action, Long operatorId,
                                     OperatorRole operatorRole, String remark, String attachmentUrls) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(action, "action must not be null");

        WorkOrderEntity entity = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("工单不存在: id=" + orderId));

        WorkOrderStatus current = entity.getStatus();
        Set<WorkOrderStatus> allowed = Optional.ofNullable(TRANSITION_RULES.get(action))
                .orElseThrow(() -> new BusinessException("未知的流转动作: " + action));

        if (!allowed.contains(current)) {
            throw new BusinessException(String.format(
                    "非法状态流转：当前状态[%s]不允许执行[%s]操作", current.getDescription(), action.getDescription()));
        }

        WorkOrderStatus target = ACTION_TARGET.get(action);
        entity.setStatus(target);
        applyTimestamp(entity, action, Instant.now());
        entity.setUpdatedAt(Instant.now());

        // 记录流转日志
        OrderTransitionEntity logEntity = new OrderTransitionEntity();
        logEntity.setOrderId(orderId);
        logEntity.setFromStatus(current);
        logEntity.setToStatus(target);
        logEntity.setAction(action);
        logEntity.setOperatorId(operatorId);
        logEntity.setOperatorRole(operatorRole != null ? operatorRole : OperatorRole.SYSTEM);
        logEntity.setRemark(remark);
        logEntity.setAttachmentUrls(attachmentUrls);
        logEntity.setCreatedAt(Instant.now());
        orderTransitionRepository.save(logEntity);

        log.info("Work order transitioned: orderId={}, {} -> {} via {}",
                orderId, current, target, action);
        return toDomain(workOrderRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTransition> getOrderHistory(Long orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return orderTransitionRepository.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(WorkOrderServiceImpl::toTransitionDomain)
                .toList();
    }

    @Override
    public WorkOrder assignWorker(Long orderId, Long workerId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        Objects.requireNonNull(workerId, "workerId must not be null");

        WorkOrderEntity entity = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("工单不存在: id=" + orderId));
        entity.setWorkerId(workerId);
        entity.setUpdatedAt(Instant.now());
        log.info("Worker assigned: orderId={}, workerId={}", orderId, workerId);
        return toDomain(workOrderRepository.save(entity));
    }

    /**
     * 根据动作设置对应的时间戳字段。
     */
    private static void applyTimestamp(WorkOrderEntity entity, TransitionAction action, Instant now) {
        switch (action) {
            case ACCEPT -> entity.setAcceptedAt(now);
            case START -> entity.setStartedAt(now);
            case SUBMIT -> entity.setSubmittedAt(now);
            case APPROVE -> entity.setCompletedAt(now);
            case CANCEL -> entity.setCancelledAt(now);
            default -> {
                // DISPATCH / REJECT / DISPUTE / SETTLE 无需单独时间戳
            }
        }
    }

    private static WorkOrder toDomain(WorkOrderEntity e) {
        return new WorkOrder(
                e.getId(), e.getOrderNo(), e.getOrderType(), e.getTitle(), e.getDescription(),
                e.getMemberId(), e.getWorkerId(), e.getChamber(), e.getAmount(), e.getStatus(),
                e.getPriority(), e.getLocationLat(), e.getLocationLng(),
                e.getScheduledAt(), e.getAcceptedAt(), e.getStartedAt(), e.getSubmittedAt(),
                e.getCompletedAt(), e.getCancelledAt(), e.getCancelReason(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }

    private static OrderTransition toTransitionDomain(OrderTransitionEntity e) {
        return new OrderTransition(
                e.getId(), e.getOrderId(), e.getFromStatus(), e.getToStatus(), e.getAction(),
                e.getOperatorId(), e.getOperatorRole(), e.getRemark(), e.getAttachmentUrls(), e.getCreatedAt()
        );
    }
}
