package com.platformcommons.finance.service;

import com.platformcommons.finance.domain.FinancialDisclosure;
import com.platformcommons.finance.domain.FinancingRecord;
import com.platformcommons.finance.domain.ProcurementRecord;
import com.platformcommons.finance.domain.RelatedPartyTransaction;

import java.util.List;
import java.util.Optional;

/**
 * 融资采购合规服务（第10-11章 第50-59条）
 * <p>
 * 负责融资合规审查、采购公开、关联交易审查、财务透明。
 */
public interface FinanceComplianceService {

    /**
     * 提交融资记录并审查合规性
     *
     * @param record 融资记录
     * @return 审查通过的记录编号
     */
    String submitFinancingRecord(FinancingRecord record);

    /**
     * 查询融资记录
     *
     * @param recordId 记录编号
     * @return 融资记录
     */
    Optional<FinancingRecord> getFinancingRecord(String recordId);

    /**
     * 提交采购记录
     *
     * @param record 采购记录
     * @return 采购记录编号
     */
    String submitProcurementRecord(ProcurementRecord record);

    /**
     * 提交关联交易记录并审查
     *
     * @param transaction 关联交易
     * @return 审查结果（true=通过）
     */
    boolean reviewRelatedPartyTransaction(RelatedPartyTransaction transaction);

    /**
     * 发布财务公开记录
     *
     * @param disclosure 财务公开
     * @return 公开编号
     */
    String publishFinancialDisclosure(FinancialDisclosure disclosure);

    /**
     * 查询财务公开记录
     *
     * @param period 财务期间
     * @return 财务公开
     */
    Optional<FinancialDisclosure> getFinancialDisclosure(String period);

    /**
     * 列出所有融资记录
     *
     * @return 融资记录列表
     */
    List<FinancingRecord> listFinancingRecords();
}
