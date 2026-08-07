package com.platformcommons.matching.api;

import com.platformcommons.matching.api.dto.AssignWorkerRequest;
import com.platformcommons.matching.api.dto.CreateOrderRequest;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.matching.api.dto.OrderTransitionResponse;
import com.platformcommons.matching.api.dto.TransitionRequest;
import com.platformcommons.matching.api.dto.WorkOrderResponse;
import com.platformcommons.matching.domain.workorder.OrderPriority;
import com.platformcommons.matching.domain.workorder.OrderTransition;
import com.platformcommons.matching.domain.workorder.OperatorRole;
import com.platformcommons.matching.domain.workorder.TransitionAction;
import com.platformcommons.matching.domain.workorder.WorkOrder;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import com.platformcommons.matching.application.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * 业务工单接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "业务订单", description = "工单全生命周期管理")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @Operation(summary = "创建工单")
    @PostMapping("/api/work-orders")
    public WorkOrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order: memberId={}, type={}", request.memberId(), request.orderType());
        WorkOrder order = workOrderService.createOrder(
                request.memberId(),
                WorkOrderType.valueOf(request.orderType().toUpperCase()),
                request.title(),
                request.description(),
                request.amount(),
                request.locationLat(),
                request.locationLng(),
                request.scheduledAt(),
                request.priority() != null
                        ? OrderPriority.valueOf(request.priority().toUpperCase())
                        : OrderPriority.NORMAL
        );
        return toResponse(order);
    }

    @Operation(summary = "按订单号查询工单")
    @GetMapping("/api/work-orders/{orderNo}")
    public WorkOrderResponse getOrder(@PathVariable String orderNo) {
        return toResponse(workOrderService.getOrder(orderNo));
    }

    @Operation(summary = "查询需求方工单列表")
    @GetMapping("/api/work-orders/member/{memberId}")
    public List<WorkOrderResponse> listMemberOrders(@PathVariable Long memberId) {
        return workOrderService.listMemberOrders(memberId).stream()
                .map(WorkOrderController::toResponse)
                .toList();
    }

    @Operation(summary = "查询劳动者工单列表")
    @GetMapping("/api/work-orders/worker/{workerId}")
    public List<WorkOrderResponse> listWorkerOrders(@PathVariable Long workerId) {
        return workOrderService.listWorkerOrders(workerId).stream()
                .map(WorkOrderController::toResponse)
                .toList();
    }

    @Operation(summary = "工单状态流转")
    @PostMapping("/api/work-orders/{orderId}/transition")
    public WorkOrderResponse transition(@PathVariable Long orderId,
                                        @Valid @RequestBody TransitionRequest request) {
        log.info("Transition order: orderId={}, action={}", orderId, request.action());
        WorkOrder order = workOrderService.transitionOrder(
                orderId,
                TransitionAction.valueOf(request.action().toUpperCase()),
                request.operatorId(),
                request.operatorRole() != null
                        ? OperatorRole.valueOf(request.operatorRole().toUpperCase())
                        : OperatorRole.SYSTEM,
                request.remark(),
                request.attachmentUrls()
        );
        return toResponse(order);
    }

    @Operation(summary = "指派劳动者")
    @PutMapping("/api/work-orders/{orderId}/assign")
    public WorkOrderResponse assignWorker(@PathVariable Long orderId,
                                          @Valid @RequestBody AssignWorkerRequest request) {
        log.info("Assign worker: orderId={}, workerId={}", orderId, request.workerId());
        return toResponse(workOrderService.assignWorker(orderId, request.workerId()));
    }

    @Operation(summary = "查询工单流转历史")
    @GetMapping("/api/work-orders/{orderId}/history")
    public List<OrderTransitionResponse> getOrderHistory(@PathVariable Long orderId) {
        return workOrderService.getOrderHistory(orderId).stream()
                .map(WorkOrderController::toTransitionResponse)
                .toList();
    }

    // ---- DTO 转换 ----

    private static WorkOrderResponse toResponse(WorkOrder o) {
        return RecordUtils.copy(o, WorkOrderResponse.class);
    }

    private static OrderTransitionResponse toTransitionResponse(OrderTransition t) {
        return RecordUtils.copy(t, OrderTransitionResponse.class);
    }
}
