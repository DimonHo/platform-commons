package com.platformcommons.payment.domain.withdrawal;

import java.time.Instant;

/**
 * 提现记录领域模型（不可变记录）。
 *
 * @param id                    记录 ID
 * @param withdrawalRequestId   提现申请 ID
 * @param channelCode           渠道编码
 * @param channelMerchant       渠道商户号
 * @param channelTransferNo     渠道转账单号
 * @param channelRespCode       渠道响应码
 * @param channelRespMsg        渠道响应消息
 * @param status                状态（DDL 字符串）
 * @param createdAt             创建时间
 * @param updatedAt             更新时间
 */
public record WithdrawalRecord(
        Long id,
        Long withdrawalRequestId,
        ChannelCode channelCode,
        String channelMerchant,
        String channelTransferNo,
        String channelRespCode,
        String channelRespMsg,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
