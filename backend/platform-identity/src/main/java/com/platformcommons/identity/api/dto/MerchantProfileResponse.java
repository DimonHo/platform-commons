package com.platformcommons.identity.api.dto;

import com.platformcommons.identity.domain.profile.ShopStatus;

/**
 * 商家档案响应 DTO。
 */
public record MerchantProfileResponse(
        Long id,
        Long memberId,
        String shopName,
        String shopCategory,
        String businessLicense,
        String licensePhotoUrl,
        String shopAddress,
        Double shopLat,
        Double shopLng,
        String businessHours,
        Integer deliveryRadiusM,
        Double rating,
        ShopStatus shopStatus
) {
}
