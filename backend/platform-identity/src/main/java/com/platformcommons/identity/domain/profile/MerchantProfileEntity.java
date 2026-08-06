package com.platformcommons.identity.domain.profile;

import com.platformcommons.identity.domain.profile.ShopStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 商家档案实体（merchant_profile 表）。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "merchant_profile")
@EqualsAndHashCode(of = "id")
public class MerchantProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_name", length = 128)
    private String shopName;

    @Column(name = "shop_category", length = 64)
    private String shopCategory;

    @Column(name = "business_license", length = 64)
    private String businessLicense;

    @Column(name = "license_photo_url", length = 512)
    private String licensePhotoUrl;

    @Column(name = "shop_address", length = 256)
    private String shopAddress;

    @Column(name = "shop_lat")
    private Double shopLat;

    @Column(name = "shop_lng")
    private Double shopLng;

    @Column(name = "business_hours", length = 128)
    private String businessHours;

    @Column(name = "delivery_radius_m")
    private Integer deliveryRadiusM;

    @Column
    private Double rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "shop_status", length = 16)
    private ShopStatus shopStatus;
}
