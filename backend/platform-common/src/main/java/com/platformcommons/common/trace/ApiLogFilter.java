package com.platformcommons.common.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * API 请求日志过滤器。
 *
 * <p>记录每个 HTTP 请求的方法、URI、来源 IP、耗时到 MDC，
 * 并在请求结束时打印请求参数（含 Body）和响应结果。</p>
 *
 * <p><b>MDC 键</b>：{@code clientIp}、{@code method}、{@code uri}、{@code elapsed}，
 * 可在日志 pattern 中引用以实现结构化日志。</p>
 *
 * <p>优先级紧随 {@link TraceIdFilter}（{@code HIGHEST_PRECEDENCE + 1}），
 * 确保 traceId 已注入 MDC 后再记录 API 信息。</p>
 *
 * <p><b>排除</b>：通过 {@link ApiLogProperties#excludePaths()}（Ant 模式）配置的路径跳过日志记录，
 * Swagger UI、Actuator 等基础设施端点可排除。</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class ApiLogFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public static final String MDC_CLIENT_IP = "clientIp";
    public static final String MDC_METHOD = "method";
    public static final String MDC_URI = "uri";
    public static final String MDC_ELAPSED = "elapsed";

    private final ApiLogProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : properties.excludePaths()) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var wrappedRequest = new ContentCachingRequestWrapper(request, 4096);
        var wrappedResponse = new ContentCachingResponseWrapper(response);

        MDC.put(MDC_CLIENT_IP, resolveClientIp(request));
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_URI, request.getRequestURI()
                + (StringUtils.hasText(request.getQueryString()) ? "?" + request.getQueryString() : ""));

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            MDC.put(MDC_ELAPSED, elapsed + "ms");
            logApi(wrappedRequest, wrappedResponse);
            clearMdc();
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logApi(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String reqBody = isMultipart(request) ? "[multipart]" : truncate(toUtf8(request.getContentAsByteArray()));
        String respBody = truncate(toUtf8(response.getContentAsByteArray()));
        log.info("API status={} | req={} | resp={}",
                response.getStatus(), reqBody, respBody);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith("multipart/");
    }

    private String toUtf8(byte[] bytes) {
        return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
    }

    private String truncate(String body) {
        int maxLen = properties.maxBodyLength();
        return body.length() > maxLen ? body.substring(0, maxLen) + "...(" + body.length() + " chars)" : body;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int comma = ip.indexOf(',');
            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private void clearMdc() {
        MDC.remove(MDC_CLIENT_IP);
        MDC.remove(MDC_METHOD);
        MDC.remove(MDC_URI);
        MDC.remove(MDC_ELAPSED);
    }
}
