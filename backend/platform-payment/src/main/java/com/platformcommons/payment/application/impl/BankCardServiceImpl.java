package com.platformcommons.payment.application.impl;

import com.platformcommons.common.api.ResultCode;
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.util.SnowflakeUtils;
import com.platformcommons.payment.domain.bankcard.CardStatus;
import com.platformcommons.payment.domain.bankcard.BankCard;
import com.platformcommons.payment.domain.bankcard.BankCardRepository;
import com.platformcommons.payment.domain.bankcard.BankCardEntity;
import com.platformcommons.payment.application.BankCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 银行卡服务实现。
 *
 * <p>演示用加密：卡号反转作为密文。生产环境应使用非对称/对称加密。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BankCardServiceImpl implements BankCardService {

    private final BankCardRepository bankCardRepository;

    @Override
    public BankCard bindCard(Long memberId, String holderName, String cardNo, String bankName, String cardType, String reservedPhone) {
        if (cardNo == null || cardNo.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "卡号不能为空");
        }
        Instant now = Instant.now();
        BankCardEntity entity = new BankCardEntity();
        entity.setMemberId(memberId);
        entity.setHolderName(holderName);
        // 卡号密文：反转字符串（演示用）
        entity.setCardNoEnc(new StringBuilder(cardNo).reverse().toString());
        // 卡号掩码：**** + 末四位
        entity.setCardNoMasked(maskCardNo(cardNo));
        entity.setBankName(bankName);
        entity.setCardType(cardType);
        entity.setReservedPhone(reservedPhone);
        entity.setIsDefault(false);
        entity.setStatus(CardStatus.ACTIVE);
        entity.setBoundAt(now);
        bankCardRepository.save(entity);
        log.info("Bank card bound: memberId={}, cardId={}, masked={}", memberId, entity.getId(), entity.getCardNoMasked());
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankCard> listCards(Long memberId) {
        return bankCardRepository.findByMemberId(memberId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void unbindCard(Long cardId) {
        BankCardEntity entity = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "银行卡不存在: id=" + cardId));
        entity.setStatus(CardStatus.UNBOUND);
        entity.setIsDefault(false);
        bankCardRepository.save(entity);
        log.info("Bank card unbound: cardId={}", cardId);
    }

    // ===== helpers =====

    private String maskCardNo(String cardNo) {
        if (cardNo == null || cardNo.length() < 4) {
            return "****";
        }
        return "****" + cardNo.substring(cardNo.length() - 4);
    }

    private BankCard toDomain(BankCardEntity e) {
        return new BankCard(e.getId(), e.getMemberId(), e.getHolderName(), e.getCardNoEnc(),
                e.getCardNoMasked(), e.getBankName(), e.getCardType(), e.getIsDefault(),
                e.getStatus(), e.getBoundAt());
    }
}
