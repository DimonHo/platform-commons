package com.platformcommons.identity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 劳动者车辆类型枚举。
 */
@Getter
@AllArgsConstructor
public enum VehicleType {
    SEDAN("轿车"), SUV("SUV"), EBIKE("电动车"), TRICYCLE("三轮车"), NONE("无");

    private final String description;
}
