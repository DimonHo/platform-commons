package com.platformcommons.governance.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建提案请求 DTO。
 *
 * @param title         标题
 * @param description   详细说明
 * @param type          提案类型（{@code POLICY_CHANGE/SETTLEMENT_RULE/...}）
 * @param proposerId    提案人成员 ID
 * @param targetChamber 目标议院（可空，表示全员表决）
 */
public record CreateProposalRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 128, message = "标题长度不能超过 128")
        String title,

        @Size(max = 2048, message = "描述长度不能超过 2048")
        String description,

        @NotBlank(message = "提案类型不能为空")
        String type,

        @NotNull(message = "提案人不能为空")
        Long proposerId,

        String targetChamber
) {
}
