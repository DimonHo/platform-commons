package com.platformcommons.payment.service;

import com.platformcommons.payment.domain.BankCard;

import java.util.List;

/**
 * 银行卡服务接口。
 */
public interface BankCardService {

    /**
     * 绑定银行卡。
     *
     * @param memberId      会员 ID
     * @param holderName    持卡人姓名
     * @param cardNo        卡号（明文）
     * @param bankName      开户行
     * @param cardType      卡类型：DEBIT / CREDIT
     * @param reservedPhone 银行预留手机号
     * @return 银行卡
     */
    BankCard bindCard(Long memberId, String holderName, String cardNo, String bankName, String cardType, String reservedPhone);

    /**
     * 按会员 ID 查询银行卡列表。
     *
     * @param memberId 会员 ID
     * @return 银行卡列表
     */
    List<BankCard> listCards(Long memberId);

    /**
     * 解绑银行卡。
     *
     * @param cardId 银行卡 ID
     */
    void unbindCard(Long cardId);
}
