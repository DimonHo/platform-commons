package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 工单状态流转请求。
 *
 * @param action         流转动作（DISPATCH / ACCEPT / START / SUBMIT / APPROVE / REJECT / CANCEL / DISPUTE / SETTLE）
 * @param operatorId     操作人 ID
 * @param operatorRole   操作人角色（MEMBER / WORKER / ADMIN / SYSTEM）
 * @param remark         备注
 * @param attachmentUrls 附件 URL
 */
public record TransitionRequest(
        @NotBlank String action,
        @NotNull Long operatorId,
        String operatorRole,
        String remark,
        String attachmentUrls
) {
}
