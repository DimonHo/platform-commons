package com.platformcommons.payment.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.payment.application.WalletService;
import com.platformcommons.payment.domain.transaction.LedgerEventEntity;
import com.platformcommons.payment.domain.transaction.LedgerEventRepository;
import com.platformcommons.payment.domain.transaction.SettlementResult;
import com.platformcommons.payment.domain.transaction.Transaction;
import com.platformcommons.payment.domain.transaction.TransactionEntity;
import com.platformcommons.payment.domain.transaction.TransactionRepository;
import com.platformcommons.payment.domain.transaction.TransactionStatus;
import com.platformcommons.payment.domain.wallet.WalletBusinessType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 结算链路单元测试（纯 Mockito，不依赖 DB / Spring 上下文）。
 *
 * <p>覆盖：charge→settle 金额守恒、charge 幂等、settle 状态校验、退款。
 * 金额全链路 BigDecimal，禁止 double。</p>
 */
@ExtendWith(MockitoExtension.class)
class SettlementFlowTest {

    /** 订单总价。 */
    private static final BigDecimal GROSS = new BigDecimal("200.00");
    /** 平台服务费率（与 {@code SettlementRule.DEFAULT} 对齐）。 */
    private static final BigDecimal FEE_RATE = new BigDecimal("0.05");
    /** 需求方会员 ID。 */
    private static final Long REQUESTER_ID = 1L;
    /** 劳动者会员 ID。 */
    private static final Long WORKER_ID = 2L;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerEventRepository ledgerEventRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void chargeThenSettle_conservesAmountAndMovesWallet() {
        String orderId = "ORDER-001";
        // charge：幂等键无历史记录
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction tx = paymentService.charge(orderId, String.valueOf(WORKER_ID), String.valueOf(REQUESTER_ID), GROSS);
        assertEquals(TransactionStatus.PENDING, tx.status(), "新建交易应为 PENDING");

        // 捕获 charge 落库的交易，供 settle 按 ID 加载
        ArgumentCaptor<TransactionEntity> chargeCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionRepository).save(chargeCaptor.capture());
        TransactionEntity charged = chargeCaptor.getValue();
        when(transactionRepository.findByIdForUpdate(charged.getId())).thenReturn(Optional.of(charged));

        SettlementResult result = paymentService.settle(charged.getId());

        // 金额守恒：gross = platformFee + workerShare（余量 0，SCALE=4 四舍五入）
        BigDecimal fee = GROSS.multiply(FEE_RATE).setScale(4, RoundingMode.HALF_UP);
        BigDecimal workerShare = GROSS.subtract(fee);
        assertEquals(workerShare, result.workerShare(), "劳动者实收应为订单总额减去平台服务费");
        assertEquals(workerShare, result.distributableSurplus(), "可分配结余应全部返还劳动者");
        assertEquals(0, GROSS.subtract(fee).subtract(workerShare).signum(), "金额守恒：gross = fee + workerShare");
        assertTrue(result.compliant(), "全额返还应满足反榨取底线");

        // 钱包入账：扣需求方订单总额，劳动者入账劳动所得
        verify(walletService).deduct(eq(REQUESTER_ID), eq(GROSS), eq(WalletBusinessType.SETTLE),
                eq("PaymentTransaction"), eq(orderId), eq("订单结算扣款"));
        verify(walletService).income(eq(WORKER_ID), eq(workerShare), eq(WalletBusinessType.SETTLE),
                eq("PaymentTransaction"), eq(orderId), eq("劳动所得入账"));

        // 落库：charge 一次 + settle 一次；账本事件两条
        verify(transactionRepository, times(2)).save(any(TransactionEntity.class));
        verify(ledgerEventRepository, times(2)).save(any(LedgerEventEntity.class));
    }

    @Test
    void charge_duplicateOrderId_throwsDataDuplicated() {
        String orderId = "ORDER-DUP";
        TransactionEntity existed = entity(orderId, TransactionStatus.PENDING);
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.of(existed));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.charge(orderId, String.valueOf(WORKER_ID), String.valueOf(REQUESTER_ID), GROSS));

        assertEquals(ResultCode.DATA_DUPLICATED.getCode(), ex.getCode(), "重复订单应报数据已存在");
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void settle_alreadySettled_throwsStatusNotAllowed() {
        TransactionEntity settled = entity("ORDER-S2", TransactionStatus.SETTLED);
        when(transactionRepository.findByIdForUpdate(settled.getId())).thenReturn(Optional.of(settled));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.settle(settled.getId()));

        assertEquals(ResultCode.STATUS_NOT_ALLOWED.getCode(), ex.getCode(), "已结算交易不可重复结算");
        // 状态校验失败，不得触发任何钱包入账
        verify(walletService, never()).deduct(any(Long.class), any(BigDecimal.class),
                any(WalletBusinessType.class), any(String.class), any(String.class), any(String.class));
        verify(walletService, never()).income(any(Long.class), any(BigDecimal.class),
                any(WalletBusinessType.class), any(String.class), any(String.class), any(String.class));
    }

    @Test
    void settleAndRefund_loadWithPessimisticLock() {
        TransactionEntity pending = entity("ORDER-LOCK", TransactionStatus.PENDING);
        when(transactionRepository.findByIdForUpdate(pending.getId())).thenReturn(Optional.of(pending));
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.settle(pending.getId());
        assertEquals(TransactionStatus.SETTLED, pending.getStatus(), "结算后状态应为 SETTLED");

        paymentService.refund(pending.getId());
        assertEquals(TransactionStatus.REFUNDED, pending.getStatus(), "退款后状态应为 REFUNDED");

        // 结算/退款加载必须走悲观锁查询（PG 行锁），不得用无锁 findById，防止并发双重结算
        verify(transactionRepository, times(2)).findByIdForUpdate(pending.getId());
        verify(transactionRepository, never()).findById(pending.getId());
    }

    @Test
    void refund_afterSettle_marksRefundedAndRejectsTwice() {
        String orderId = "ORDER-R1";
        when(transactionRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.charge(orderId, String.valueOf(WORKER_ID), String.valueOf(REQUESTER_ID), GROSS);

        ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionRepository).save(captor.capture());
        TransactionEntity charged = captor.getValue();
        when(transactionRepository.findByIdForUpdate(charged.getId())).thenReturn(Optional.of(charged));

        paymentService.settle(charged.getId());
        assertEquals(TransactionStatus.SETTLED, charged.getStatus(), "结算后状态应为 SETTLED");

        Transaction refunded = paymentService.refund(charged.getId());
        assertEquals(TransactionStatus.REFUNDED, refunded.status(), "退款后状态应为 REFUNDED");

        // 已退款交易不可再次退款
        BusinessException ex = assertThrows(BusinessException.class,
                () -> paymentService.refund(charged.getId()));
        assertEquals(ResultCode.STATUS_NOT_ALLOWED.getCode(), ex.getCode(), "重复退款应报状态不允许");

        // 落库：charge + settle + refund 各一次
        verify(transactionRepository, times(3)).save(any(TransactionEntity.class));
    }

    // ===== 测试工具 =====

    private static TransactionEntity entity(String orderId, TransactionStatus status) {
        TransactionEntity e = new TransactionEntity();
        e.setId(UUID.randomUUID());
        e.setOrderId(orderId);
        e.setWorkerId(String.valueOf(WORKER_ID));
        e.setRequesterId(String.valueOf(REQUESTER_ID));
        e.setGrossAmount(GROSS);
        e.setPlatformFee(BigDecimal.ZERO);
        e.setWorkerShare(BigDecimal.ZERO);
        e.setStatus(status);
        e.setRuleVersion("v1-2026");
        e.setCreatedAt(Instant.now());
        return e;
    }
}
