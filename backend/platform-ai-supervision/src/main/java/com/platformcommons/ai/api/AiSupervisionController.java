package com.platformcommons.ai.api;

import com.platformcommons.ai.api.dto.ReviewRequest;
import com.platformcommons.ai.api.dto.ReviewResponse;
import com.platformcommons.ai.domain.ReviewResult;
import com.platformcommons.ai.service.AiSupervisionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * AI 公共监督审议 Controller（第12章 第60-69条）
 */
@RestController
@RequestMapping("/api/ai-supervision")
public class AiSupervisionController {

    private static final Logger log = LoggerFactory.getLogger(AiSupervisionController.class);

    private final AiSupervisionService aiSupervisionService;

    public AiSupervisionController(AiSupervisionService aiSupervisionService) {
        this.aiSupervisionService = aiSupervisionService;
    }

    /**
     * 发起并执行审议
     */
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("收到审议请求: item={}", request.mandatoryItem());
        String reviewId = aiSupervisionService.initiateReview(request.mandatoryItem(), request.proposal());
        ReviewResult result = aiSupervisionService.conductReview(reviewId);
        ReviewResponse response = new ReviewResponse(
                result.reviewId(),
                result.beneficiaries(),
                result.costBearers(),
                result.alternativeProposal(),
                result.dissentingViews(),
                result.consensusReached(),
                result.summary()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 查询审议详情
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable String reviewId) {
        return aiSupervisionService.getReviewStatus(reviewId)
                .map(status -> ResponseEntity.ok(new ReviewResponse(reviewId, Set.of(), Set.of(), null, Set.of(), true, "状态: " + status.getDescription())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询所有审议记录
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponse>> listReviews() {
        List<ReviewResponse> responses = aiSupervisionService.listAllReviews().stream()
                .map(r -> new ReviewResponse(r.reviewId(), r.beneficiaries(), r.costBearers(), r.alternativeProposal(), r.dissentingViews(), r.consensusReached(), r.summary()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * 提交审议争议
     */
    @PostMapping("/reviews/{reviewId}/contest")
    public ResponseEntity<Void> contestReview(@PathVariable String reviewId, @RequestBody String dissent) {
        log.info("收到争议提交: reviewId={}", reviewId);
        aiSupervisionService.contestReview(reviewId, dissent);
        return ResponseEntity.ok().build();
    }
}
