package com.platformcommons.dispute.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 提交争议请求 DTO
 *
 * @param filedBy     申诉人编号
 * @param subject     争议事由
 * @param description 详细描述
 */
public record FileDisputeRequest(
        @NotBlank(message = "申诉人不能为空")
        String filedBy,

        @NotBlank(message = "争议事由不能为空")
        String subject,

        @NotBlank(message = "详细描述不能为空")
        String description
) implements Serializable {
}
