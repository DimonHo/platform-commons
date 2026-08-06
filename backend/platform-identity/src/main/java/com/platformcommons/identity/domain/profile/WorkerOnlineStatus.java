package com.platformcommons.identity.domain.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 劳动者在线状态枚举。
 */
@Getter
@AllArgsConstructor
public enum WorkerOnlineStatus {
    ONLINE("在线"), OFFLINE("离线"), BUSY("忙碌");

    private final String description;
}
