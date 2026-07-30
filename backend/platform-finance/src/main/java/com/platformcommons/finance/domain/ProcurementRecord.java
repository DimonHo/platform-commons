package com.platformcommons.finance.domain;

import java.math.BigDecimal;

/**
 * 采购记录（第11章 第55-57条）
 * <p>
 * 采购须公开：供应商、金额、评标过程、实际受益人。
 *
 * @param recordId     记录编号
 * @param supplier     供应商名称
 * @param amount       采购金额
 * @param evaluation   评标结果摘要
 * @param beneficiary  实际受益人（需穿透核查）
 * @param disclosedAt  公开日期
 */
public record ProcurementRecord(
        String recordId,
        String supplier,
        BigDecimal amount,
        String evaluation,
        String beneficiary,
        String disclosedAt
) {
}
