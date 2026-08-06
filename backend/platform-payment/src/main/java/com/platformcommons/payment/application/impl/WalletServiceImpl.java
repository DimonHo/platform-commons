package com.platformcommons.payment.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.payment.domain.wallet.TransactionDirection;
import com.platformcommons.payment.domain.wallet.Wallet;
import com.platformcommons.payment.domain.wallet.WalletBusinessType;
import com.platformcommons.payment.domain.wallet.WalletStatus;
import com.platformcommons.payment.domain.wallet.WalletTransaction;
import com.platformcommons.payment.domain.wallet.WalletRepository;
import com.platformcommons.payment.domain.wallet.WalletTransactionRepository;
import com.platformcommons.payment.domain.wallet.WalletEntity;
import com.platformcommons.payment.domain.wallet.WalletTransactionEntity;
import com.platformcommons.payment.application.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 钱包服务实现。
 *
 * <p>阿里规范：金额比较必须使用 {@link BigDecimal#compareTo(Object)}，禁止使用 {@code equals()}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    public Wallet getOrCreateWallet(Long memberId) {
        WalletEntity entity = walletRepository.findByMemberId(memberId).orElseGet(() -> {
            Instant now = Instant.now();
            WalletEntity created = new WalletEntity();
            created.setMemberId(memberId);
            created.setBalance(BigDecimal.ZERO);
            created.setFrozenAmount(BigDecimal.ZERO);
            created.setStatus(WalletStatus.ACTIVE);
            created.setCreatedAt(now);
            created.setUpdatedAt(now);
            return walletRepository.save(created);
        });
        return toDomain(entity);
    }

    @Override
    public WalletTransaction recharge(Long memberId, BigDecimal amount, String remark) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "充值金额必须为正数");
        }
        WalletEntity wallet = requireWalletByMember(memberId);
        ensureOperable(wallet);

        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        WalletTransactionEntity tx = newTransaction(
                wallet, memberId, TransactionDirection.IN, amount, newBalance,
                WalletBusinessType.RECHARGE, null, null, remark
        );
        walletTransactionRepository.save(tx);
        log.info("Wallet recharged: memberId={}, amount={}, balanceAfter={}", memberId, amount, newBalance);
        return toDomain(tx);
    }

    @Override
    public WalletTransaction freeze(Long memberId, BigDecimal amount, String refType, String refId) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "冻结金额必须为正数");
        }
        WalletEntity wallet = requireWalletByMember(memberId);
        ensureOperable(wallet);

        // 余额检查：balance - frozenAmount >= amount
        BigDecimal available = wallet.getBalance().subtract(wallet.getFrozenAmount());
        if (available.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "可用余额不足，无法冻结");
        }

        // 从余额转移到冻结金额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        WalletTransactionEntity tx = newTransaction(
                wallet, memberId, TransactionDirection.OUT, amount, wallet.getBalance(),
                WalletBusinessType.WITHDRAW, refType, refId, "冻结金额"
        );
        walletTransactionRepository.save(tx);
        log.info("Wallet frozen: memberId={}, amount={}, refType={}, refId={}", memberId, amount, refType, refId);
        return toDomain(tx);
    }

    @Override
    public WalletTransaction deduct(Long memberId, BigDecimal amount, WalletBusinessType businessType, String refType, String refId, String remark) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "扣款金额必须为正数");
        }
        WalletEntity wallet = requireWalletByMember(memberId);
        ensureOperable(wallet);

        // 余额检查：balance - frozenAmount >= amount
        BigDecimal available = wallet.getBalance().subtract(wallet.getFrozenAmount());
        if (available.compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "可用余额不足，无法扣款");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        WalletTransactionEntity tx = newTransaction(
                wallet, memberId, TransactionDirection.OUT, amount, newBalance,
                businessType, refType, refId, remark
        );
        walletTransactionRepository.save(tx);
        log.info("Wallet deducted: memberId={}, amount={}, balanceAfter={}, businessType={}", memberId, amount, newBalance, businessType);
        return toDomain(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransaction> listTransactions(Long walletId) {
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void freezeWallet(Long memberId) {
        WalletEntity wallet = requireWalletByMember(memberId);
        wallet.setStatus(WalletStatus.FROZEN);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        log.info("Wallet status frozen: memberId={}", memberId);
    }

    @Override
    public void unfreezeWallet(Long memberId) {
        WalletEntity wallet = requireWalletByMember(memberId);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        log.info("Wallet status unfrozen: memberId={}", memberId);
    }

    // ===== helpers =====

    private WalletEntity requireWalletByMember(Long memberId) {
        return walletRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "钱包不存在: memberId=" + memberId));
    }

    private void ensureOperable(WalletEntity wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new BusinessException(ResultCode.STATUS_NOT_ALLOWED, "钱包状态不可操作: " + wallet.getStatus());
        }
    }

    private WalletTransactionEntity newTransaction(WalletEntity wallet, Long memberId,
                                                   TransactionDirection direction, BigDecimal amount, BigDecimal balanceAfter,
                                                   WalletBusinessType businessType, String refType, String refId, String remark) {
        WalletTransactionEntity tx = new WalletTransactionEntity();
        tx.setWalletId(wallet.getId());
        tx.setMemberId(memberId);
        tx.setTransactionNo(SnowflakeUtils.nextId("WT"));
        tx.setDirection(direction);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setBusinessType(businessType);
        tx.setRefType(refType);
        tx.setRefId(refId);
        tx.setRemark(remark);
        tx.setCreatedAt(Instant.now());
        return tx;
    }

    private Wallet toDomain(WalletEntity e) {
        return new Wallet(e.getId(), e.getMemberId(), e.getBalance(), e.getFrozenAmount(),
                e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private WalletTransaction toDomain(WalletTransactionEntity e) {
        return new WalletTransaction(e.getId(), e.getWalletId(), e.getMemberId(), e.getTransactionNo(),
                e.getDirection(), e.getAmount(), e.getBalanceAfter(), e.getBusinessType(),
                e.getRefType(), e.getRefId(), e.getRemark(), e.getCreatedAt());
    }
}
