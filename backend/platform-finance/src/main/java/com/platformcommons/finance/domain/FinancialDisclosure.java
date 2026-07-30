package com.platformcommons.finance.domain;

import java.math.BigDecimal;

/**
 * 财务公开记录（第11章 第59条）
 * <p>
 * 平台定期公开财务数据，包括：收入、支出、结余、资金流向。
 *
 * @param disclosureId  公开编号
 * @param period        财务期间（如 "2026-Q1"）
 * @param totalRevenue  总收入
 * @param totalExpenditure 总支出
 * @param surplus       结余
 * @param fundFlowSummary 资金流向摘要
 * @param disclosedAt   公开时间
 */
public record FinancialDisclosure(
        String disclosureId,
        String period,
        BigDecimal totalRevenue,
        BigDecimal totalExpenditure,
        BigDecimal surplus,
        String fundFlowSummary,
        String disclosedAt
) {
}
