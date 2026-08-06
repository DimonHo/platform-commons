package com.platformcommons.payment.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道路由状态。
 */
@Getter
@AllArgsConstructor
public enum ChannelRouteStatus {

    PENDING("处理中"),
    SUCCESS("成功"),
    FAIL("失败"),
    CLOSED("已关闭");

    private final String description;
}
