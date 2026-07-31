package com.platformcommons.ai.api;

import com.platformcommons.ai.api.dto.ReviewRequest;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.ai.api.dto.ReviewResponse;
import com.platformcommons.ai.domain.ReviewResult;
import com.platformcommons.ai.service.AiSupervisionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 公共监督审议 Controller（第12章 第60-69条）
 *
 * <p>方法返回裸 DTO，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@RestController
@RequestMapping("/api/ai-supervision")
@Slf4j
public class AiSupervisionController {


    private final AiSupervisionService aiSupervisionService;

    public AiSupervisionController(AiSupervisionService aiSupervisionService) {
        this.aiSupervisionService = aiSupervisionService;
    }

    /**
     * 发起并执行审议
     */
    @PostMapping("/reviews")
    public ReviewResponse createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("收到审议请求: item={}", request.mandatoryItem());
        String reviewId = aiSupervisionService.initiateReview(request.mandatoryItem(), request.proposal());
        ReviewResult result = aiSupervisionService.conductReview(reviewId);
        return RecordUtils.copy(result, ReviewResponse.class);
    }

    /**
     * 查询审议详情
     */
    @GetMapping("/reviews/{reviewId}")
    public ReviewResponse getReview(@PathVariable String reviewId) {
        return aiSupervisionService.getReviewStatus(reviewId)
                .map(status -> new ReviewResponse(reviewId, Set.of(), Set.of(), null, Set.of(), true, "状态: " + status.getDescription()))
                .orElseThrow(() -> new com.platformcommons.common.exception.BusinessException(
                        com.platformcommons.common.api.ResultCode.DATA_NOT_FOUND,
                        "审议不存在: " + reviewId));
    }

    /**
     * 查询所有审议记录
     */
    @GetMapping("/reviews")
    public List<ReviewResponse> listReviews() {
        return aiSupervisionService.listAllReviews().stream()
                .map(r -> RecordUtils.copy(r, ReviewResponse.class))
                .toList();
    }

    /**
     * 提交审议争议
     */
    @PostMapping("/reviews/{reviewId}/contest")
    public void contestReview(@PathVariable String reviewId, @RequestBody String dissent) {
        log.info("收到争议提交: reviewId={}", reviewId);
        aiSupervisionService.contestReview(reviewId, dissent);
    }
}
