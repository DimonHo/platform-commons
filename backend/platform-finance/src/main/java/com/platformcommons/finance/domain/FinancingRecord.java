package com.platformcommons.finance.domain;

import java.math.BigDecimal;

/**
 * 融资记录（第10章 第50-54条）
 * <p>
 * 平台融资必须满足以下约束：
 * - 设定偿付上限，防止资本回报无限膨胀
 * - 声明融资方无平台治理权（资本与治理分离）
 * - 公开融资条款供全体成员审计
 *
 * @param recordId        记录编号
 * @param amount          融资金额
 * @param financingType   融资类型（债权/股权/可转债等）
 * @param repaymentCap    偿付上限（含本息）
 * @param noGovernance    无治理权声明（融资方不获得治理投票权）
 * @param disclosedAt     公开日期
 */
public record FinancingRecord(
        String recordId,
        BigDecimal amount,
        String financingType,
        BigDecimal repaymentCap,
        boolean noGovernance,
        String disclosedAt
) {
}
