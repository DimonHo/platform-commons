package com.platformcommons.common.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 响应包装配置属性。
 *
 * <p>在 {@code application.yml} 中配置：</p>
 * <pre>{@code
 * platformcommons:
 *   response:
 *     exclude-paths:
 *       - /v3/api-docs/**
 *       - /swagger-ui/**
 * }</pre>
 *
 * @param excludePaths 排除路径列表（Ant 风格），匹配到的请求路径跳过自动包装
 */
@ConfigurationProperties(prefix = "platformcommons.response")
public record ResponseWrapProperties(
        List<String> excludePaths
) {

    /**
     * 紧凑构造器：保证 {@code excludePaths} 非 null。
     */
    public ResponseWrapProperties {
        if (excludePaths == null) {
            excludePaths = List.of();
        }
    }
}
