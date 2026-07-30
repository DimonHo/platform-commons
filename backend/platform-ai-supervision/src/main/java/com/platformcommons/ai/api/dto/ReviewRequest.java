package com.platformcommons.ai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 审议请求 DTO
 *
 * @param mandatoryItem 强制审议事项
 * @param proposal      待审议提案内容
 */
public record ReviewRequest(
        @NotNull(message = "审议事项不能为空")
        com.platformcommons.ai.domain.MandatoryReviewItem mandatoryItem,

        @NotBlank(message = "提案内容不能为空")
        String proposal
) implements Serializable {
}
