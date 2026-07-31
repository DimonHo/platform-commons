package com.platformcommons.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

/**
 * 全局响应体自动包装。
 *
 * <p>Controller 方法只需返回裸业务对象（DTO / Domain），本 Advice 统一将其包装为 {@link R}，
 * 实现业务代码与响应结构解耦——不再需要在每个 Controller 里手写 {@code R.success(xxx)}。</p>
 *
 * <p>对以下情况跳过自动包装：
 * <ul>
 *   <li>已经是 {@link R} 类型——避免重复包装</li>
 *   <li>{@code String} ——Spring MVC 对 String 有特殊处理，且多为错误页/纯文本场景</li>
 *   <li>{@code byte[]} ——二进制流</li>
 *   <li>请求路径匹配 {@code platformcommons.response.exclude-paths} 配置的 Ant 模式
 *       ——Swagger UI、Actuator health 等基础设施端点的原始响应需原样返回</li>
 * </ul>
 * </p>
 *
 * <p><b>注意</b>：Controller 方法若返回 {@code ResponseEntity<T>}，Spring 会先拆包取 body 再进入本 Advice，
 * 因此 {@code ResponseEntity<Proposal>} 的 Proposal 仍会被自动包装为 {@code R<Proposal>}。</p>
 */
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(GlobalResponseAdvice.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 通过配置注入的排除路径（Ant 风格），匹配到的请求路径跳过包装。 */
    private final List<String> excludePaths;

    /**
     * @param properties 响应包装配置属性，可为 {@code null}（无配置时全部包装）
     */
    public GlobalResponseAdvice(ResponseWrapProperties properties) {
        this.excludePaths = (properties != null && properties.excludePaths() != null)
                ? properties.excludePaths()
                : List.of();
    }

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
        // 配置排除路径（Ant 模式匹配）
        String path = request.getURI().getPath();
        if (isExcluded(path)) {
            return body;
        }
        if (body == null) {
            return R.success();
        }
        log.debug("自动包装响应：{} → {}", body.getClass().getSimpleName(), path);
        return R.success(body);
    }

    /**
     * 判断请求路径是否在排除列表中。
     *
     * @param path 请求路径（如 {@code /v3/api-docs}）
     * @return 命中任一 Ant 模式则 {@code true}
     */
    private boolean isExcluded(String path) {
        for (String pattern : excludePaths) {
            if (PATH_MATCHER.match(pattern, path)) {
                log.debug("路径 [{}] 命中排除模式 [{}]，跳过包装", path, pattern);
                return true;
            }
        }
        return false;
    }
}
