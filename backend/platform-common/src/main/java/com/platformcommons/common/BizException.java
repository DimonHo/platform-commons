package com.platformcommons.common;

import com.platformcommons.common.api.ResultCode;

/**
 * 业务异常基类。
 *
 * <p>阿里规范：业务异常应当使用自定义异常体系，不要直接抛出 RuntimeException。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 默认错误码。 */
    public static final int DEFAULT_CODE = ResultCode.BUSINESS_ERROR.getCode();

    private final int code;

    /**
     * 构造业务异常。
     *
     * @param message 异常信息
     */
    public BizException(String message) {
        this(DEFAULT_CODE, message);
    }

    /**
     * 构造业务异常。
     *
     * @param code    错误码
     * @param message 异常信息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常。
     *
     * @param message 异常信息
     * @param cause   原因
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    /**
     * 获取错误码。
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
