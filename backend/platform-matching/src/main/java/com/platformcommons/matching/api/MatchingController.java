package com.platformcommons.matching.api;

import com.platformcommons.matching.api.dto.MatchRequest;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.matching.api.dto.MatchResponse;
import com.platformcommons.matching.domain.MatchResult;
import com.platformcommons.matching.service.MatchingEngineService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 匹配引擎接口。
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingEngineService matchingEngineService;

    /**
     * 执行匹配。
     *
     * @param request 匹配请求
     * @return 匹配结果
     */
    @PostMapping("/api/matching/match")
    public MatchResponse match(@Valid @RequestBody MatchRequest request) {
        log.info("Match request: taskId={}, strategy={}", request.taskId(), request.strategyName());
        MatchResult result = matchingEngineService.match(request.taskId(), request.strategyName());
        return RecordUtils.copy(result, MatchResponse.class);
    }

    /**
     * 注册劳动者位置。
     *
     * @param workerId         劳动者 ID
     * @param latitude         纬度
     * @param longitude        经度
     * @param activeOrders     活跃订单数
     * @param rating           评分
     * @param registrationDays 注册天数
     * @return 操作结果（void 会被包装为 {@code R<Void>}）
     */
    @PostMapping("/api/matching/workers")
    public void register(@RequestParam String workerId,
                         @RequestParam double latitude,
                         @RequestParam double longitude,
                         @RequestParam(defaultValue = "0") int activeOrders,
                         @RequestParam(defaultValue = "5.0") double rating,
                         @RequestParam(defaultValue = "0") int registrationDays) {
        log.info("Register worker: workerId={}, activeOrders={}", workerId, activeOrders);
        matchingEngineService.registerWorker(workerId, latitude, longitude, activeOrders, rating, registrationDays);
    }

    /**
     * 列出所有已注册劳动者。
     *
     * @return 劳动者 ID 列表
     */
    @GetMapping("/api/matching/workers")
    public List<String> listWorkers() {
        return matchingEngineService.listWorkers();
    }
}
