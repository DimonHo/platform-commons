package com.platformcommons.payment.api;

import com.platformcommons.payment.api.dto.WithdrawalRequestDto;
import com.platformcommons.common.util.RecordUtils;
import com.platformcommons.payment.api.dto.WithdrawalResponse;
import com.platformcommons.payment.domain.withdrawal.WithdrawalRequest;
import com.platformcommons.payment.application.WithdrawalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提现管理 REST 接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawals")
@Tag(name = "提现管理")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public WithdrawalResponse requestWithdrawal(@Valid @RequestBody WithdrawalRequestDto request) {
        WithdrawalRequest wr = withdrawalService.requestWithdrawal(request.memberId(), request.bankCardId(), request.amount());
        return RecordUtils.copy(wr, WithdrawalResponse.class);
    }

    @PutMapping("/{id}/approve")
    public WithdrawalResponse approve(@PathVariable Long id, @RequestParam Long reviewerId) {
        WithdrawalRequest wr = withdrawalService.approveWithdrawal(id, reviewerId);
        return RecordUtils.copy(wr, WithdrawalResponse.class);
    }

    @PutMapping("/{id}/reject")
    public WithdrawalResponse reject(@PathVariable Long id, @RequestParam Long reviewerId, @RequestParam String reason) {
        WithdrawalRequest wr = withdrawalService.rejectWithdrawal(id, reviewerId, reason);
        return RecordUtils.copy(wr, WithdrawalResponse.class);
    }

    @PutMapping("/{id}/complete")
    public WithdrawalResponse complete(@PathVariable Long id, @RequestParam String channelTransferNo) {
        WithdrawalRequest wr = withdrawalService.completeWithdrawal(id, channelTransferNo);
        return RecordUtils.copy(wr, WithdrawalResponse.class);
    }

    @GetMapping("/member/{memberId}")
    public List<WithdrawalResponse> listMemberWithdrawals(@PathVariable Long memberId) {
        return withdrawalService.listMemberWithdrawals(memberId).stream()
                .map(w -> RecordUtils.copy(w, WithdrawalResponse.class))
                .toList();
    }

}
