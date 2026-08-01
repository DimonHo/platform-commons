package com.platformcommons.common.trace;

import com.platformcommons.common.util.SnowflakeUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 链路追踪 ID 过滤器。
 *
 * <p>每个 HTTP 请求入口处生成（或透传上游）traceId，写入 {@link TraceContext}（MDC）供全链路日志关联，
 * 并通过响应头 {@code X-Trace-Id} 返回给调用方。</p>
 *
 * <p><b>优先级最高</b>：确保后续所有 Filter / Interceptor / Controller 的日志都带上 traceId。</p>
 *
 * <p><b>上游透传</b>：若请求头携带 {@code X-Trace-Id}，则直接沿用（适配 API 网关 / 微服务场景）。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** traceId 请求/响应头名称。 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = SnowflakeUtils.nextId();
        }
        TraceContext.setTraceId(traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceContext.clear();
        }
    }
}
