package com.platformcommons.payment.api;

import com.platformcommons.payment.api.dto.FreezeRequest;
import com.platformcommons.payment.api.dto.RechargeRequest;
import com.platformcommons.payment.api.dto.WalletResponse;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.payment.api.dto.WalletTransactionResponse;
import com.platformcommons.payment.domain.wallet.Wallet;
import com.platformcommons.payment.domain.wallet.WalletTransaction;
import com.platformcommons.payment.application.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包管理 REST 接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
@Tag(name = "钱包管理")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{memberId}")
    public WalletResponse getOrCreateWallet(@PathVariable Long memberId) {
        Wallet wallet = walletService.getOrCreateWallet(memberId);
        return new WalletResponse(wallet.id(), wallet.memberId(), wallet.balance(), wallet.frozenAmount(),
                wallet.status(), wallet.createdAt(), wallet.updatedAt());
    }

    @PostMapping("/recharge")
    public WalletTransactionResponse recharge(@Valid @RequestBody RechargeRequest request) {
        WalletTransaction tx = walletService.recharge(request.memberId(), request.amount(), null);
        return RecordUtils.copy(tx, WalletTransactionResponse.class);
    }

    @PostMapping("/freeze")
    public WalletTransactionResponse freeze(@Valid @RequestBody FreezeRequest request) {
        WalletTransaction tx = walletService.freeze(request.memberId(), request.amount(), request.refType(), request.refId());
        return RecordUtils.copy(tx, WalletTransactionResponse.class);
    }

    @GetMapping("/{memberId}/transactions")
    public List<WalletTransactionResponse> listTransactions(@PathVariable Long memberId) {
        // 先确保钱包存在，再查询流水
        Wallet wallet = walletService.getOrCreateWallet(memberId);
        return walletService.listTransactions(wallet.id()).stream()
                .map(t -> RecordUtils.copy(t, WalletTransactionResponse.class))
                .toList();
    }

}
