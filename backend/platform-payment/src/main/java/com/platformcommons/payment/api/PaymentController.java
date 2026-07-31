package com.platformcommons.payment.api;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.payment.api.dto.ChargeRequest;
import com.platformcommons.payment.api.dto.SettlementResponse;
import com.platformcommons.payment.domain.SettlementResult;
import com.platformcommons.payment.domain.Transaction;
import com.platformcommons.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 支付与分账接口。
 *
 * <p>阿里规范：Controller 层只负责参数校验与调度，不写业务逻辑。
 * 方法返回裸领域对象，由 {@code GlobalResponseAdvice} 自动包装。</p>
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 创建收款交易。
     *
     * @param request 收款请求
     * @return 交易
     */
    @PostMapping("/charge")
    public Transaction charge(@Valid @RequestBody ChargeRequest request) {
        log.info("Charge request: orderId={}", request.orderId());
        return paymentService.charge(
                request.orderId(), request.workerId(), request.requesterId(), request.grossAmount());
    }

    /**
     * 结算分账。
     *
     * @param transactionId 交易 ID
     * @return 结算结果
     */
    @PostMapping("/settle/{transactionId}")
    public SettlementResponse settle(@PathVariable UUID transactionId) {
        log.info("Settle request: txId={}", transactionId);
        SettlementResult result = paymentService.settle(transactionId);
        return toResponse(result);
    }

    /**
     * 发起退款。
     *
     * @param transactionId 交易 ID
     * @return 退款后的交易
     */
    @PostMapping("/refund/{transactionId}")
    public Transaction refund(@PathVariable UUID transactionId) {
        log.info("Refund request: txId={}", transactionId);
        return paymentService.refund(transactionId);
    }

    /**
     * 查询交易详情。
     *
     * @param transactionId 交易 ID
     * @return 交易
     */
    @GetMapping("/{transactionId}")
    public Transaction get(@PathVariable UUID transactionId) {
        return paymentService.findById(transactionId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND,
                        "交易不存在: " + transactionId));
    }

    private static SettlementResponse toResponse(SettlementResult r) {
        return new SettlementResponse(
                r.transactionId(), r.grossAmount(), r.platformFee(), r.distributableSurplus(),
                r.workerShare(), r.workerShareRatio(), r.ruleVersion(), r.compliant()
        );
    }
}
