package com.platformcommons.payment.api;

import com.platformcommons.common.Result;
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
 * <p>阿里规范：Controller 层只负责参数校验与结果包装，不写业务逻辑。
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
    public Result<Transaction> charge(@Valid @RequestBody ChargeRequest request) {
        log.info("Charge request: orderId={}", request.orderId());
        Transaction tx = paymentService.charge(
                request.orderId(), request.workerId(), request.requesterId(), request.grossAmount());
        return Result.success(tx);
    }

    /**
     * 结算分账。
     *
     * @param transactionId 交易 ID
     * @return 结算结果
     */
    @PostMapping("/settle/{transactionId}")
    public Result<SettlementResponse> settle(@PathVariable UUID transactionId) {
        log.info("Settle request: txId={}", transactionId);
        SettlementResult result = paymentService.settle(transactionId);
        return Result.success(toResponse(result));
    }

    /**
     * 发起退款。
     *
     * @param transactionId 交易 ID
     * @return 退款后的交易
     */
    @PostMapping("/refund/{transactionId}")
    public Result<Transaction> refund(@PathVariable UUID transactionId) {
        log.info("Refund request: txId={}", transactionId);
        Transaction tx = paymentService.refund(transactionId);
        return Result.success(tx);
    }

    /**
     * 查询交易详情。
     *
     * @param transactionId 交易 ID
     * @return 交易
     */
    @GetMapping("/{transactionId}")
    public Result<Transaction> get(@PathVariable UUID transactionId) {
        return paymentService.findById(transactionId)
                .map(Result::success)
                .orElseGet(() -> Result.failure("transaction not found: " + transactionId));
    }

    private static SettlementResponse toResponse(SettlementResult r) {
        return new SettlementResponse(
                r.transactionId(), r.grossAmount(), r.platformFee(), r.distributableSurplus(),
                r.workerShare(), r.workerShareRatio(), r.ruleVersion(), r.compliant()
        );
    }
}
