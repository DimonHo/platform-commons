package com.platformcommons.common;

/**
 * 统一返回结果包装类。
 *
 * <p>阿里规范：POJO 类必须提供 toString；此处为 record 自带等价能力。
 *
 * @param code    业务状态码
 * @param message 提示信息
 * @param data    载荷
 * @param <T>     载荷类型
 */
public record Result<T>(int code, String message, T data) {

    /** 成功状态码。 */
    public static final int CODE_SUCCESS = 0;

    /** 失败状态码。 */
    public static final int CODE_FAILURE = 1;

    /**
     * 构造成功结果。
     *
     * @param data 载荷
     * @param <T>  载荷类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "success", data);
    }

    /**
     * 构造成功结果（无载荷）。
     *
     * @param <T> 载荷类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "success", null);
    }

    /**
     * 构造失败结果。
     *
     * @param message 失败信息
     * @param <T>     载荷类型
     * @return 失败结果
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(CODE_FAILURE, message, null);
    }
}
