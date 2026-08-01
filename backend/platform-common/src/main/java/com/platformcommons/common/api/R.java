package com.platformcommons.common.api;

import com.platformcommons.common.trace.TraceContext;
import java.io.Serializable;

/**
 * 统一响应体封装。
 *
 * <p>所有对外接口统一返回此结构，避免裸数据暴露，便于前端统一处理。
 * Controller 方法返回裸业务对象，由 {@code GlobalResponseAdvice}（{@link org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice})
 * 自动包装为本类型。</p>
 *
 * @param code      业务状态码，{@code 0} 表示成功，其他为错误码
 * @param message   提示信息
 * @param data      业务数据
 * @param traceId   链路追踪 ID，用于全链路日志关联
 * @param timestamp 响应生成时间戳（毫秒）
 * @param <T>       业务数据类型
 */
public record R<T>(int code, String message, T data, String traceId, long timestamp) implements Serializable {

    /**
     * 成功（无数据）。
     */
    public static <T> R<T> success() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 成功（带数据）。
     */
    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 成功（带数据和自定义提示）。
     */
    public static <T> R<T> success(T data, String message) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 失败（按错误码）。
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 失败（按错误码并覆盖提示信息）。
     */
    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 失败（自定义错误码与提示）。
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, TraceContext.getTraceId(), System.currentTimeMillis());
    }

    /**
     * 是否成功。
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }
}
