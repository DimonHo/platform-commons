package com.platformcommons.common.api;

import java.io.Serializable;

/**
 * 统一响应体封装。
 *
 * <p>所有对外接口统一返回此结构，避免裸数据暴露，便于前端统一处理。</p>
 *
 * @param code      业务状态码，{@code 0} 表示成功，其他为错误码
 * @param message   提示信息
 * @param data      业务数据
 * @param timestamp 响应生成时间戳（毫秒）
 * @param <T>       业务数据类型
 */
public record Result<T>(int code, String message, T data, long timestamp) implements Serializable {

    /**
     * 成功（无数据）。
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, System.currentTimeMillis());
    }

    /**
     * 成功（带数据）。
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, System.currentTimeMillis());
    }

    /**
     * 成功（带数据和自定义提示）。
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data, System.currentTimeMillis());
    }

    /**
     * 失败（按错误码）。
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, System.currentTimeMillis());
    }

    /**
     * 失败（按错误码并覆盖提示信息）。
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null, System.currentTimeMillis());
    }

    /**
     * 失败（自定义错误码与提示）。
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    /**
     * 是否成功。
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }
}
