package com.platformcommons.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务类型枚举。
 */
@AllArgsConstructor
@Getter
public enum ServiceType {

    RIDE_HAILING("网约车"),
    FOOD_DELIVERY("即时配送"),
    HOUSEKEEPING("家政服务"),
    ERRAND("跑腿代办");

    private final String displayName;

}