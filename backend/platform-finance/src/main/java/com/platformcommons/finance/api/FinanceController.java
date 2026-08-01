package com.platformcommons.finance.api;

import com.platformcommons.finance.api.dto.FinancingReviewRequest;
import com.platformcommons.finance.domain.FinancialDisclosure;
import com.platformcommons.finance.domain.FinancingRecord;
import com.platformcommons.finance.service.FinanceComplianceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 融资采购关联交易 Controller（第10-11章 第50-59条）
 *
 * <p>方法返回裸对象，由 {@code GlobalResponseAdvice} 自动包装。
 * 原本返回 String 的方法改为返回 {@code Map}，避免 String 被 Spring MVC 特殊处理。</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceComplianceService financeComplianceService;

    /**
     * 提交融资审查
     */
    @PostMapping("/api/finance/financing")
    public Map<String, String> submitFinancing(@Valid @RequestBody FinancingReviewRequest request) {
        log.info("收到融资审查请求: amount={}, type={}", request.amount(), request.financingType());
        FinancingRecord record = new FinancingRecord(
                null,
                request.amount(),
                request.financingType(),
                request.repaymentCap(),
                request.noGovernance(),
                Instant.now().toString()
        );
        String recordId = financeComplianceService.submitFinancingRecord(record);
        return Map.of("recordId", recordId);
    }

    /**
     * 查询所有融资记录
     */
    @GetMapping("/api/finance/financing")
    public List<FinancingRecord> listFinancing() {
        return financeComplianceService.listFinancingRecords();
    }

    /**
     * 发布财务公开
     */
    @PostMapping("/api/finance/disclosures")
    public Map<String, String> publishDisclosure(@RequestBody FinancialDisclosure disclosure) {
        log.info("收到财务公开发布: period={}", disclosure.period());
        String disclosureId = financeComplianceService.publishFinancialDisclosure(disclosure);
        return Map.of("disclosureId", disclosureId);
    }
}
