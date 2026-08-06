package com.platformcommons.identity.api.dto;

import java.time.Instant;

/**
 * 收货地址响应 DTO。
 */
public record AddressResponse(
        Long id,
        Long memberId,
        String label,
        String receiverName,
        String phone,
        String province,
        String city,
        String district,
        String detail,
        Double latitude,
        Double longitude,
        Boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
