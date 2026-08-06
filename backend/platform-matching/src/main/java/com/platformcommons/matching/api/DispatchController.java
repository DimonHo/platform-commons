package com.platformcommons.matching.api;

import com.platformcommons.matching.api.dto.CreateBroadcastRequest;
import com.platformcommons.matching.api.dto.DispatchBroadcastResponse;
import com.platformcommons.matching.api.dto.DispatchGrabRecordResponse;
import com.platformcommons.matching.api.dto.GrabOrderRequest;
import com.platformcommons.matching.domain.dispatch.BroadcastType;
import com.platformcommons.matching.domain.dispatch.DispatchBroadcast;
import com.platformcommons.matching.domain.dispatch.DispatchGrabRecord;
import com.platformcommons.matching.domain.workorder.WorkOrderType;
import com.platformcommons.matching.application.DispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * 派单广播接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "派单广播", description = "抢单/系统指派派单")
public class DispatchController {

    private final DispatchService dispatchService;

    @Operation(summary = "创建派单广播")
    @PostMapping("/api/dispatch/broadcasts")
    public DispatchBroadcastResponse createBroadcast(@Valid @RequestBody CreateBroadcastRequest request) {
        log.info("Create broadcast: orderId={}, type={}", request.orderId(), request.broadcastType());
        DispatchBroadcast broadcast = dispatchService.createBroadcast(
                request.orderId(),
                WorkOrderType.valueOf(request.orderType().toUpperCase()),
                request.centerLat(),
                request.centerLng(),
                request.radiusMeters(),
                request.targetCount(),
                BroadcastType.valueOf(request.broadcastType().toUpperCase())
        );
        return toResponse(broadcast);
    }

    @Operation(summary = "劳动者抢单")
    @PostMapping("/api/dispatch/broadcasts/{broadcastId}/grab")
    public DispatchGrabRecordResponse grabOrder(@PathVariable Long broadcastId,
                                                @Valid @RequestBody GrabOrderRequest request) {
        log.info("Grab order: broadcastId={}, workerId={}", broadcastId, request.workerId());
        DispatchGrabRecord record = dispatchService.grabOrder(
                broadcastId, request.workerId(), request.workerLat(), request.workerLng()
        );
        return toResponse(record);
    }

    @Operation(summary = "按广播号查询广播")
    @GetMapping("/api/dispatch/broadcasts/{broadcastNo}")
    public DispatchBroadcastResponse getBroadcast(@PathVariable String broadcastNo) {
        return toResponse(dispatchService.getBroadcast(broadcastNo));
    }

    @Operation(summary = "列出广播中的活跃广播")
    @GetMapping("/api/dispatch/broadcasts/active")
    public List<DispatchBroadcastResponse> listActiveBroadcasts() {
        return dispatchService.listActiveBroadcasts().stream()
                .map(DispatchController::toResponse)
                .toList();
    }

    @Operation(summary = "查询广播的抢单记录")
    @GetMapping("/api/dispatch/broadcasts/{broadcastId}/grabs")
    public List<DispatchGrabRecordResponse> listGrabRecords(@PathVariable Long broadcastId) {
        return dispatchService.listGrabRecords(broadcastId).stream()
                .map(DispatchController::toResponse)
                .toList();
    }

    // ---- DTO 转换 ----

    private static DispatchBroadcastResponse toResponse(DispatchBroadcast b) {
        return new DispatchBroadcastResponse(
                b.id(), b.broadcastNo(), b.orderId(),
                b.orderType() != null ? b.orderType().name() : null,
                b.broadcastType() != null ? b.broadcastType().name() : null,
                b.centerLat(), b.centerLng(), b.radiusMeters(), b.targetCount(),
                b.grabbedCount(),
                b.status() != null ? b.status().name() : null,
                b.expireAt(), b.createdAt()
        );
    }

    private static DispatchGrabRecordResponse toResponse(DispatchGrabRecord r) {
        return new DispatchGrabRecordResponse(
                r.id(), r.broadcastId(), r.workerId(),
                r.workerLat(), r.workerLng(), r.distanceMeters(),
                r.status() != null ? r.status().name() : null,
                r.grabbedAt()
        );
    }
}
