package com.platformcommons.payment.api;

import com.platformcommons.payment.api.dto.BankCardResponse;
import com.platformcommons.payment.api.dto.BindCardRequest;
import com.platformcommons.payment.domain.bankcard.BankCard;
import com.platformcommons.payment.application.BankCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 银行卡管理 REST 接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank-cards")
@Tag(name = "银行卡管理")
public class BankCardController {

    private final BankCardService bankCardService;

    @PostMapping
    public BankCardResponse bindCard(@Valid @RequestBody BindCardRequest request) {
        BankCard card = bankCardService.bindCard(request.memberId(), request.holderName(),
                request.cardNo(), request.bankName(), request.cardType(), request.reservedPhone());
        return toResponse(card);
    }

    @GetMapping("/{memberId}")
    public List<BankCardResponse> listCards(@PathVariable Long memberId) {
        return bankCardService.listCards(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{cardId}")
    public void unbindCard(@PathVariable Long cardId) {
        bankCardService.unbindCard(cardId);
    }

    private BankCardResponse toResponse(BankCard card) {
        return new BankCardResponse(card.id(), card.memberId(), card.holderName(),
                card.cardNoMasked(), card.bankName(), card.cardType(),
                card.isDefault(), card.status(), card.boundAt());
    }
}
