package com.platformcommons.finance.api;

import com.platformcommons.finance.api.dto.FinancingReviewRequest;
import com.platformcommons.finance.domain.FinancialDisclosure;
import com.platformcommons.finance.domain.FinancingRecord;
import com.platformcommons.finance.service.FinanceComplianceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * 融资采购关联交易 Controller（第10-11章 第50-59条）
 */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private static final Logger log = LoggerFactory.getLogger(FinanceController.class);

    private final FinanceComplianceService financeComplianceService;

    public FinanceController(FinanceComplianceService financeComplianceService) {
        this.financeComplianceService = financeComplianceService;
    }

    /**
     * 提交融资审查
     */
    @PostMapping("/financing")
    public ResponseEntity<String> submitFinancing(@Valid @RequestBody FinancingReviewRequest request) {
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
        return ResponseEntity.ok(recordId);
    }

    /**
     * 查询所有融资记录
     */
    @GetMapping("/financing")
    public ResponseEntity<List<FinancingRecord>> listFinancing() {
        return ResponseEntity.ok(financeComplianceService.listFinancingRecords());
    }

    /**
     * 发布财务公开
     */
    @PostMapping("/disclosures")
    public ResponseEntity<String> publishDisclosure(@RequestBody FinancialDisclosure disclosure) {
        log.info("收到财务公开发布: period={}", disclosure.period());
        String disclosureId = financeComplianceService.publishFinancialDisclosure(disclosure);
        return ResponseEntity.ok(disclosureId);
    }
}
