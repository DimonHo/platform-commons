package com.platformcommons.finance.service.impl;

import com.platformcommons.finance.domain.FinancialDisclosure;
import com.platformcommons.finance.domain.FinancingRecord;
import com.platformcommons.finance.domain.ProcurementRecord;
import com.platformcommons.finance.domain.RelatedPartyTransaction;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.finance.repository.FinancingRecordRepository;
import com.platformcommons.finance.repository.entity.FinancingRecordEntity;
import com.platformcommons.finance.service.FinanceComplianceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

/**
 * 融资采购合规服务实现（第10-11章 第50-59条）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FinanceComplianceServiceImpl implements FinanceComplianceService {


    private final FinancingRecordRepository financingRecordRepository;
    private final List<ProcurementRecord> procurementRecords = new CopyOnWriteArrayList<>();
    private final List<RelatedPartyTransaction> relatedPartyTransactions = new CopyOnWriteArrayList<>();
    private final List<FinancialDisclosure> financialDisclosures = new CopyOnWriteArrayList<>();


    @Override
    @Transactional
    public String submitFinancingRecord(FinancingRecord record) {
        String recordId = Optional.ofNullable(record.recordId()).orElseGet(SnowflakeUtils::nextId);
        log.info("提交融资记录: recordId={}, amount={}, type={}", recordId, record.amount(), record.financingType());

        // 合规审查：必须有偿付上限和无治理权声明
        if (record.repaymentCap() == null || record.repaymentCap().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("融资记录必须设定偿付上限");
        }
        if (!record.noGovernance()) {
            throw new IllegalArgumentException("融资方必须声明无平台治理权");
        }

        FinancingRecordEntity entity = new FinancingRecordEntity();
        entity.setRecordId(recordId);
        entity.setAmount(record.amount());
        entity.setFinancingType(record.financingType());
        entity.setRepaymentCap(record.repaymentCap());
        entity.setNoGovernance(record.noGovernance());
        entity.setDisclosedAt(record.disclosedAt());
        financingRecordRepository.save(entity);

        return recordId;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FinancingRecord> getFinancingRecord(String recordId) {
        return financingRecordRepository.findByRecordId(recordId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public String submitProcurementRecord(ProcurementRecord record) {
        String recordId = Optional.ofNullable(record.recordId()).orElseGet(SnowflakeUtils::nextId);
        log.info("提交采购记录: recordId={}, supplier={}, amount={}", recordId, record.supplier(), record.amount());
        ProcurementRecord saved = new ProcurementRecord(
                recordId, record.supplier(), record.amount(), record.evaluation(), record.beneficiary(), record.disclosedAt()
        );
        procurementRecords.add(saved);
        return recordId;
    }

    @Override
    @Transactional
    public boolean reviewRelatedPartyTransaction(RelatedPartyTransaction transaction) {
        String recordId = Optional.ofNullable(transaction.recordId()).orElseGet(SnowflakeUtils::nextId);
        log.info("审查关联交易: recordId={}, counterparty={}, relatedParty={}", recordId, transaction.counterparty(), transaction.relatedParty());

        // 关联交易必须披露关联关系，且金额超过阈值须审查
        boolean approved = StringUtils.hasText(transaction.relationship());
        RelatedPartyTransaction reviewed = new RelatedPartyTransaction(
                recordId, transaction.counterparty(), transaction.relatedParty(),
                transaction.transactionAmount(), transaction.relationship(), approved
        );
        relatedPartyTransactions.add(reviewed);
        log.info("关联交易审查结果: recordId={}, approved={}", recordId, approved);
        return approved;
    }

    @Override
    @Transactional
    public String publishFinancialDisclosure(FinancialDisclosure disclosure) {
        String disclosureId = Optional.ofNullable(disclosure.disclosureId()).orElseGet(SnowflakeUtils::nextId);
        log.info("发布财务公开: disclosureId={}, period={}", disclosureId, disclosure.period());
        FinancialDisclosure saved = new FinancialDisclosure(
                disclosureId, disclosure.period(), disclosure.totalRevenue(),
                disclosure.totalExpenditure(), disclosure.surplus(),
                disclosure.fundFlowSummary(), disclosure.disclosedAt()
        );
        financialDisclosures.add(saved);
        return disclosureId;
    }

    @Override
    public Optional<FinancialDisclosure> getFinancialDisclosure(String period) {
        return financialDisclosures.stream()
                .filter(d -> d.period().equalsIgnoreCase(period))
                .findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinancingRecord> listFinancingRecords() {
        return financingRecordRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private FinancingRecord toDomain(FinancingRecordEntity entity) {
        return new FinancingRecord(
                entity.getRecordId(),
                entity.getAmount(),
                entity.getFinancingType(),
                entity.getRepaymentCap(),
                Boolean.TRUE.equals(entity.getNoGovernance()),
                entity.getDisclosedAt()
        );
    }
}
