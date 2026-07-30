package com.platformcommons.common.enums;

/**
 * 服务类型枚举。
 */
public enum ServiceType {

    RIDE_HAILING("网约车"),
    FOOD_DELIVERY("即时配送"),
    HOUSEKEEPING("家政服务"),
    ERRAND("跑腿代办");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
