package com.platformcommons.common.exception;

import com.platformcommons.common.api.Result;
import com.platformcommons.common.api.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 *
 * <p>统一捕获各类异常并转为 {@link Result}，避免堆栈直接暴露给客户端。
 * 日志统一使用 SLF4J，占位符 {@code {}} 拼接。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：返回 HTTP 200，业务码标记失败。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常：code={}, message={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 服务异常：返回 HTTP 500。
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Result<Void>> handleServiceException(ServiceException ex) {
        log.error("服务异常：code={}, message={}", ex.getCode(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * @RequestBody 参数校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验异常：{}", message);
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(ResultCode.PARAM_INVALID, message));
    }

    /**
     * 表单参数绑定失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定异常：{}", message);
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(ResultCode.PARAM_INVALID, message));
    }

    /**
     * 约束校验失败（@RequestParam/@PathVariable 上的校验）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("约束校验异常：{}", message);
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(ResultCode.PARAM_INVALID, message));
    }

    /**
     * 必需参数缺失。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("参数缺失：{}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.fail(ResultCode.PARAM_MISSING, "缺少必需参数：" + ex.getParameterName()));
    }

    /**
     * 参数类型不匹配。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配：{}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK)
                .body(Result.fail(ResultCode.PARAM_INVALID, "参数类型不匹配：" + ex.getName()));
    }

    /**
     * 请求路由不存在。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("资源不存在：{}", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail(ResultCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 兜底：未预期的异常。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex) {
        log.error("系统未捕获异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ResultCode.INTERNAL_ERROR, ex.getMessage()));
    }
}
