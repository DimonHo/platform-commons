package com.platformcommons.finance.domain;

import java.math.BigDecimal;

/**
 * 关联交易记录（第11章 第58条）
 * <p>
 * 关联交易需特别披露：交易对手是否与理事会成员/管理层存在关联关系。
 *
 * @param recordId       记录编号
 * @param counterparty   交易对手方
 * @param relatedParty   关联方（理事会成员/管理层）
 * @param transactionAmount 交易金额
 * @param relationship   关联关系描述
 * @param approved       是否经关联交易审查批准
 */
public record RelatedPartyTransaction(
        String recordId,
        String counterparty,
        String relatedParty,
        BigDecimal transactionAmount,
        String relationship,
        boolean approved
) {
}
