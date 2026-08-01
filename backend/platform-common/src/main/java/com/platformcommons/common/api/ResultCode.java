package com.platformcommons.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举。
 *
 * <p>命名遵循阿里规范：常量使用全大写 + 下划线分隔（UPPER_SNAKE_CASE）。
 * 编码分段：{@code 0} 成功；{@code 1xxxx} 通用客户端错误；
 * {@code 5xxxx} 服务端错误；{@code 6xxxx} 业务错误。</p>
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),

    // 通用客户端错误 1xxxx
    PARAM_INVALID(10000, "参数无效"),
    PARAM_MISSING(10001, "参数缺失"),
    UNAUTHORIZED(10002, "未授权"),
    FORBIDDEN(10003, "禁止访问"),
    RESOURCE_NOT_FOUND(10004, "资源不存在"),
    METHOD_NOT_ALLOWED(10005, "请求方法不支持"),
    REQUEST_TIMEOUT(10006, "请求超时"),
    TOO_MANY_REQUESTS(10007, "请求过于频繁"),

    // 服务端错误 5xxxx
    INTERNAL_ERROR(50000, "系统内部错误"),
    SERVICE_UNAVAILABLE(50001, "服务不可用"),

    // 业务错误 6xxxx
    BUSINESS_ERROR(60000, "业务处理失败"),
    DATA_NOT_FOUND(60001, "数据不存在"),
    DATA_DUPLICATED(60002, "数据已存在"),
    STATUS_NOT_ALLOWED(60003, "当前状态不允许此操作");

    private final int code;
    private final String message;

}