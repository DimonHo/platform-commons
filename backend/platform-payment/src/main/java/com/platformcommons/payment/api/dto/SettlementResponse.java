package com.platformcommons.payment.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 结算结果响应 DTO。
 *
 * @param transactionId         交易 ID
 * @param grossAmount           订单总价
 * @param platformFee           平台服务费
 * @param distributableSurplus  可分配结余
 * @param workerShare           劳动者返还
 * @param workerShareRatio      劳动者返还比例
 * @param ruleVersion           分账规则版本
 * @param compliant             是否满足反榨取底线
 */
public record SettlementResponse(
        UUID transactionId,
        BigDecimal grossAmount,
        BigDecimal platformFee,
        BigDecimal distributableSurplus,
        BigDecimal workerShare,
        BigDecimal workerShareRatio,
        String ruleVersion,
        boolean compliant
) {
}
