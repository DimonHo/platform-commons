package com.platformcommons.payment.application;

import com.platformcommons.payment.domain.transaction.SettlementResult;
import com.platformcommons.payment.domain.transaction.Transaction;

import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 支付与分账服务接口。
 *
 * <p>阿里规范：业务逻辑层必须以接口形式对外提供能力。
 */
public interface PaymentService {

    /**
     * 创建交易并收款（订单总价公开透明，平台服务费按规则计算）。
     *
     * @param orderId     订单 ID
     * @param workerId    劳动者 ID
     * @param requesterId 发包方 ID
     * @param grossAmount 订单总价
     * @return 已创建的交易
     */
    Transaction charge(String orderId, String workerId, String requesterId, BigDecimal grossAmount);

    /**
     * 按当前生效的分账规则进行结算，确保劳动者返还不低于反榨取底线。
     *
     * @param transactionId 交易 ID
     * @return 结算结果
     */
    SettlementResult settle(UUID transactionId);

    /**
     * 发起退款。
     *
     * @param transactionId 交易 ID
     * @return 退款后的交易
     */
    Transaction refund(UUID transactionId);

    /**
     * 查询交易详情。
     *
     * @param transactionId 交易 ID
     * @return 交易（可能不存在）
     */
    Optional<Transaction> findById(UUID transactionId);
}
