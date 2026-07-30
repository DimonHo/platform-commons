package com.platformcommons.common.exception;

import com.platformcommons.common.api.ResultCode;

/**
 * 服务异常。
 *
 * <p>用于 Service 层表达非预期故障（如依赖不可用、数据一致性被破坏），
 * 区别于 {@link BusinessException} 的可预期业务规则违反。</p>
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public ServiceException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }

    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
