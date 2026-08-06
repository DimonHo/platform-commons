package com.platformcommons.identity.domain.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商家店铺营业状态枚举。
 */
@Getter
@AllArgsConstructor
public enum ShopStatus {
    OPEN("营业中"), CLOSED("已打烊");

    private final String description;
}
