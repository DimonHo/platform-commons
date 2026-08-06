package com.platformcommons.payment.service;

import com.platformcommons.payment.domain.WithdrawalRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提现服务接口。
 */
public interface WithdrawalService {

    /**
     * 申请提现。
     *
     * @param memberId   会员 ID
     * @param bankCardId 银行卡 ID
     * @param amount     提现金额
     * @return 提现申请
     */
    WithdrawalRequest requestWithdrawal(Long memberId, Long bankCardId, BigDecimal amount);

    /**
     * 审核通过提现申请。
     *
     * @param withdrawalId 提现申请 ID
     * @param reviewerId   审核人 ID
     * @return 提现申请
     */
    WithdrawalRequest approveWithdrawal(Long withdrawalId, Long reviewerId);

    /**
     * 拒绝提现申请。
     *
     * @param withdrawalId 提现申请 ID
     * @param reviewerId   审核人 ID
     * @param reason       拒绝原因
     * @return 提现申请
     */
    WithdrawalRequest rejectWithdrawal(Long withdrawalId, Long reviewerId, String reason);

    /**
     * 完成提现（调用渠道转账并扣减钱包）。
     *
     * @param withdrawalId      提现申请 ID
     * @param channelTransferNo 渠道转账单号
     * @return 提现申请
     */
    WithdrawalRequest completeWithdrawal(Long withdrawalId, String channelTransferNo);

    /**
     * 按会员 ID 查询提现申请列表。
     *
     * @param memberId 会员 ID
     * @return 提现申请列表
     */
    List<WithdrawalRequest> listMemberWithdrawals(Long memberId);
}
