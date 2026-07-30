package com.platformcommons.governance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 投票请求 DTO。
 *
 * @param voterId 投票人成员 ID
 * @param choice  投票选择（{@code YES/NO/ABSTAIN}）
 */
public record VoteRequest(
        @NotNull(message = "投票人不能为空")
        Long voterId,

        @NotBlank(message = "投票选择不能为空")
        String choice
) {
}
