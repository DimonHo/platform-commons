package com.platformcommons.identity.domain;

/**
 * 商家档案领域模型（不可变 record）。
 *
 * @param id               主键
 * @param memberId         成员 ID
 * @param shopName         店铺名称
 * @param shopCategory     店铺分类
 * @param businessLicense  营业执照号
 * @param licensePhotoUrl  执照照片 URL
 * @param shopAddress      店铺地址
 * @param shopLat          纬度
 * @param shopLng          经度
 * @param businessHours    营业时间
 * @param deliveryRadiusM  配送半径（米）
 * @param rating           评分
 * @param shopStatus       店铺状态
 */
public record MerchantProfile(
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
