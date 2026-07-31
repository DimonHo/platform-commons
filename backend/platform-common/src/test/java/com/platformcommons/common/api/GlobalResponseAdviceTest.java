package com.platformcommons.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link GlobalResponseAdvice} 单元测试。
 *
 * <p>验证配置排除路径（Ant 模式）能正确跳过包装，普通路径正常包装。</p>
 */
class GlobalResponseAdviceTest {

    /** 用裸对象模拟 Controller 返回值。 */
    private record SampleDto(String name) {}

    private GlobalResponseAdvice adviceWith(List<String> excludes) {
        return new GlobalResponseAdvice(new ResponseWrapProperties(excludes));
    }

    private ServerHttpRequest requestWithPath(String path) {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(path));
        return request;
    }

    @Test
    void normalPath_shouldWrap() {
        var advice = adviceWith(List.of("/actuator/**"));
        var body = new SampleDto("test");

        Object result = advice.beforeBodyWrite(
                body, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/api/members/1"),
                mock(ServerHttpResponse.class)
        );

        assertInstanceOf(R.class, result);
        assertSame(body, ((R<?>) result).data());
    }

    @Test
    void actuatorPath_shouldSkip() {
        var advice = adviceWith(List.of("/actuator/**"));
        var body = new SampleDto("test");

        Object result = advice.beforeBodyWrite(
                body, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/actuator/health"),
                mock(ServerHttpResponse.class)
        );

        assertSame(body, result, "actuator 路径应原样返回，不包装");
    }

    @Test
    void swaggerPaths_shouldSkip() {
        var advice = adviceWith(List.of(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        ));

        // /v3/api-docs
        assertSame(42, advice.beforeBodyWrite(
                42, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/v3/api-docs"),
                mock(ServerHttpResponse.class)
        ));

        // /swagger-ui/index.html
        assertSame("doc", advice.beforeBodyWrite(
                "doc", null, MediaType.TEXT_HTML, null,
                requestWithPath("/swagger-ui/index.html"),
                mock(ServerHttpResponse.class)
        ));

        // /swagger-ui.html
        assertSame("ui", advice.beforeBodyWrite(
                "ui", null, MediaType.TEXT_HTML, null,
                requestWithPath("/swagger-ui.html"),
                mock(ServerHttpResponse.class)
        ));
    }

    @Test
    void nullExcludePaths_shouldNotNpe() {
        var advice = new GlobalResponseAdvice(new ResponseWrapProperties(null));

        Object result = advice.beforeBodyWrite(
                new SampleDto("test"), null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/anything"),
                mock(ServerHttpResponse.class)
        );

        assertInstanceOf(R.class, result, "null 配置应等价于空列表，正常包装");
    }

    @Test
    void nullBody_nonExcludedPath_wrapsAsSuccess() {
        var advice = adviceWith(List.of("/actuator/**"));

        Object result = advice.beforeBodyWrite(
                null, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/api/test"),
                mock(ServerHttpResponse.class)
        );

        assertInstanceOf(R.class, result);
        assertNull(((R<?>) result).data());
        assertEquals(0, ((R<?>) result).code());
    }

    @Test
    void nullBody_excludedPath_returnsNull() {
        var advice = adviceWith(List.of("/actuator/**"));

        Object result = advice.beforeBodyWrite(
                null, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/actuator/health"),
                mock(ServerHttpResponse.class)
        );

        assertNull(result, "排除路径应原样返回 null");
    }

    @Test
    void wildcardPattern_matchesSubpaths() {
        var advice = adviceWith(List.of("/api/docs/**"));

        // 子路径匹配
        assertSame("x", advice.beforeBodyWrite(
                "x", null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/api/docs/v1/schema"),
                mock(ServerHttpResponse.class)
        ));

        // 非匹配路径正常包装
        Object result = advice.beforeBodyWrite(
                99, null, MediaType.APPLICATION_JSON, null,
                requestWithPath("/api/members"),
                mock(ServerHttpResponse.class)
        );
        assertInstanceOf(R.class, result);
    }
}
