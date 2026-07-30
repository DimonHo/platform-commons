package com.platformcommons.mutual.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交理赔申请 DTO。
 *
 * @param applicantId  申请人 ID
 * @param incidentType 事故类型
 * @param description  事故描述
 * @param claimedAmount 申请理赔金额
 * @param evidenceUrls 证据 URL 列表（至少一个）
 */
public record SubmitClaimRequest(
        @NotBlank(message = "applicantId must not be blank")
        String applicantId,

        @NotBlank(message = "incidentType must not be blank")
        String incidentType,

        String description,

        @NotNull(message = "claimedAmount must not be null")
        @DecimalMin(value = "0.01", message = "claimedAmount must be >= 0.01")
        BigDecimal claimedAmount,

        @NotEmpty(message = "evidenceUrls must not be empty")
        List<@NotBlank String> evidenceUrls
) {
}
