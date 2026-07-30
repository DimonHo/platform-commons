package com.platformcommons.payment.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 交易记录。
 *
 * <p>映射宪章第9章 43-49条：记录每一笔订单的定价、平台服务费、可分配结余与劳动者返还。
 *
 * @param id                   交易 ID
 * @param orderId              关联订单 ID
 * @param workerId             劳动者 ID
 * @param requesterId          发包方 ID
 * @param grossAmount          订单总价（含税前）
 * @param platformFee          平台服务费
 * @param workerShare          劳动者返还（不低于反榨取底线）
 * @param status               交易状态
 * @param settlementRuleVersion 记账时使用的分账规则版本
 * @param createdAt            创建时间
 * @param settledAt            结算完成时间
 */
public record Transaction(
        UUID id,
        String orderId,
        String workerId,
        String requesterId,
        BigDecimal grossAmount,
        BigDecimal platformFee,
        BigDecimal workerShare,
        TransactionStatus status,
        String settlementRuleVersion,
        Instant createdAt,
        Instant settledAt
) {

    /**
     * 计算可分配结余：grossAmount - platformFee。
     *
     * @return 可分配结余
     */
    public BigDecimal distributableSurplus() {
        return grossAmount.subtract(platformFee);
    }

    /**
     * 计算劳动者实际分配比例。
     *
     * @return 比例（0-1 之间），订单无金额时返回 0
     */
    public BigDecimal workerShareRatio() {
        if (grossAmount == null || grossAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return workerShare.divide(grossAmount, 4, java.math.RoundingMode.HALF_UP);
    }
}
