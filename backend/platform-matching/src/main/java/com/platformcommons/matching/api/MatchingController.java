package com.platformcommons.matching.api;

import com.platformcommons.common.Result;
import com.platformcommons.matching.api.dto.MatchRequest;
import com.platformcommons.matching.api.dto.MatchResponse;
import com.platformcommons.matching.domain.MatchResult;
import com.platformcommons.matching.service.MatchingEngineService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 匹配引擎接口。
 */
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private final MatchingEngineService matchingEngineService;

    public MatchingController(MatchingEngineService matchingEngineService) {
        this.matchingEngineService = matchingEngineService;
    }

    /**
     * 执行匹配。
     *
     * @param request 匹配请求
     * @return 匹配结果
     */
    @PostMapping("/match")
    public Result<MatchResponse> match(@Valid @RequestBody MatchRequest request) {
        log.info("Match request: taskId={}, strategy={}", request.taskId(), request.strategyName());
        MatchResult result = matchingEngineService.match(request.taskId(), request.strategyName());
        return Result.success(toResponse(result));
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
     * @return 操作结果
     */
    @PostMapping("/workers")
    public Result<Void> register(@RequestParam String workerId,
                                 @RequestParam double latitude,
                                 @RequestParam double longitude,
                                 @RequestParam(defaultValue = "0") int activeOrders,
                                 @RequestParam(defaultValue = "5.0") double rating,
                                 @RequestParam(defaultValue = "0") int registrationDays) {
        log.info("Register worker: workerId={}, activeOrders={}", workerId, activeOrders);
        matchingEngineService.registerWorker(workerId, latitude, longitude, activeOrders, rating, registrationDays);
        return Result.success();
    }

    /**
     * 列出所有已注册劳动者。
     *
     * @return 劳动者 ID 列表
     */
    @GetMapping("/workers")
    public Result<java.util.List<String>> listWorkers() {
        return Result.success(matchingEngineService.listWorkers());
    }

    private static MatchResponse toResponse(MatchResult r) {
        return new MatchResponse(r.taskId(), r.matchedWorkers(), r.strategyName(), r.explanation());
    }
}
