package com.platformcommons.common.trace;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * API 日志配置属性。
 *
 * <p>在 {@code application.yml} 中配置：</p>
 * <pre>{@code
 * platformcommons:
 *   api-log:
 *     exclude-paths:
 *       - /actuator/**
 *       - /swagger-ui/**
 *     max-body-length: 2000
 * }</pre>
 *
 * @param excludePaths  排除路径列表（Ant 风格），匹配到的请求路径跳过日志记录
 * @param maxBodyLength 单次请求/响应体日志的最大字符数，超出部分截断
 */
@ConfigurationProperties(prefix = "platformcommons.api-log")
public record ApiLogProperties(
        List<String> excludePaths,
        Integer maxBodyLength
) {

    public ApiLogProperties {
        if (excludePaths == null) {
            excludePaths = List.of();
        }
        if (maxBodyLength == null) {
            maxBodyLength = 2000;
        }
    }
}
