package com.platformcommons.payment.application;

import com.platformcommons.payment.domain.wallet.Wallet;
import com.platformcommons.payment.domain.wallet.WalletBusinessType;
import com.platformcommons.payment.domain.wallet.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务接口。
 */
public interface WalletService {

    /**
     * 获取或创建钱包。
     *
     * @param memberId 会员 ID
     * @return 钱包
     */
    Wallet getOrCreateWallet(Long memberId);

    /**
     * 充值。
     *
     * @param memberId 会员 ID
     * @param amount   充值金额
     * @param remark   备注
     * @return 钱包流水
     */
    WalletTransaction recharge(Long memberId, BigDecimal amount, String remark);

    /**
     * 冻结金额（从余额转移到冻结金额）。
     *
     * @param memberId 会员 ID
     * @param amount   冻结金额
     * @param refType  关联业务类型
     * @param refId    关联业务 ID
     * @return 钱包流水
     */
    WalletTransaction freeze(Long memberId, BigDecimal amount, String refType, String refId);

    /**
     * 扣款。
     *
     * @param memberId     会员 ID
     * @param amount       扣款金额
     * @param businessType 业务类型
     * @param refType      关联业务类型
     * @param refId        关联业务 ID
     * @param remark       备注
     * @return 钱包流水
     */
    WalletTransaction deduct(Long memberId, BigDecimal amount, WalletBusinessType businessType, String refType, String refId, String remark);

    /**
     * 查询钱包流水列表。
     *
     * @param walletId 钱包 ID
     * @return 流水列表
     */
    List<WalletTransaction> listTransactions(Long walletId);

    /**
     * 冻结钱包（整体冻结，不可用）。
     *
     * @param memberId 会员 ID
     */
    void freezeWallet(Long memberId);

    /**
     * 解冻钱包。
     *
     * @param memberId 会员 ID
     */
    void unfreezeWallet(Long memberId);
}
