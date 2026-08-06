package com.platformcommons.payment.domain.transaction;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.UUID;

/**
 * 账本事件（sealed interface）。
 *
 * <p>映射宪章第9章 49条：每一笔分账事件均需可审计、可追溯。
 * 仅允许三种具体事件，禁止外部随意扩展。
 */
public sealed interface LedgerEvent permits LedgerEvent.ChargeCreated, LedgerEvent.SettlementCompleted, LedgerEvent.RefundIssued {

    /** 事件 ID。 */
    UUID eventId();

    /** 关联交易 ID。 */
    UUID transactionId();

    /** 事件发生时间。 */
    Instant occurredAt();

    /** 事件类型名称（用于持久化区分）。 */
    String eventType();

    /**
     * 收款事件：用户支付订单总价。
     *
     * @param grossAmount 订单总价
     */
    record ChargeCreated(
            UUID eventId,
            UUID transactionId,
            BigDecimal grossAmount,
            Instant occurredAt
    ) implements LedgerEvent {
        @Override
        public String eventType() {
            return "CHARGE_CREATED";
        }
    }

    /**
     * 结算完成事件：劳动者返还已入账。
     *
     * @param workerShare 劳动者返还金额
     */
    record SettlementCompleted(
            UUID eventId,
            UUID transactionId,
            BigDecimal workerShare,
            Instant occurredAt
    ) implements LedgerEvent {
        @Override
        public String eventType() {
            return "SETTLEMENT_COMPLETED";
        }
    }

    /**
     * 退款事件：订单退款。
     *
     * @param refundAmount 退款金额
     */
    record RefundIssued(
            UUID eventId,
            UUID transactionId,
            BigDecimal refundAmount,
            Instant occurredAt
    ) implements LedgerEvent {
        @Override
        public String eventType() {
            return "REFUND_ISSUED";
        }
    }
}
