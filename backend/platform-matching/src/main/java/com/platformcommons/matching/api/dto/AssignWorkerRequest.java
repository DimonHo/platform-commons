package com.platformcommons.matching.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 指派劳动者请求。
 *
 * @param workerId 劳动者 ID
 */
public record AssignWorkerRequest(
        @NotNull Long workerId
) {
}
