package com.platformcommons.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应体自动包装。
 *
 * <p>Controller 方法只需返回裸业务对象（DTO / Domain），本 Advice 统一将其包装为 {@link R}，
 * 实现业务代码与响应结构解耦——不再需要在每个 Controller 里手写 {@code R.success(xxx)}。</p>
 *
 * <p>对以下返回类型跳过自动包装：
 * <ul>
 *   <li>已经是 {@link R} 类型——避免重复包装</li>
 *   <li>{@code ResponseEntity} ——保留 HTTP 语义（状态码 / Header）</li>
 *   <li>{@code String} ——Spring MVC 对 String 有特殊处理，且多为错误页/纯文本场景</li>
 *   <li>{@code byte[]} / 资源 ——二进制流</li>
 * </ul>
 * </p>
 *
 * <p><b>注意</b>：Controller 方法若返回 {@code ResponseEntity<T>}，Spring 会先拆包取 body 再进入本 Advice，
 * 因此 {@code ResponseEntity<Proposal>} 的 Proposal 仍会被自动包装为 {@code R<Proposal>}。</p>
 */
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(GlobalResponseAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // R 类型不重复包装
        if (R.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // String 类型跳过——Spring MVC 的 StringHttpMessageConverter 有特殊优先级
        if (CharSequence.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // 二进制跳过
        if (byte[].class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return R.success();
        }
        log.debug("自动包装响应：{}", body.getClass().getSimpleName());
        return R.success(body);
    }
}
