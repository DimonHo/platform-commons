package com.platformcommons.common.trace;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * API 日志相关 Bean 配置。
 *
 * <p>显式注册 {@link ApiLogProperties} 为 Spring Bean，
 * 供 {@link ApiLogFilter} 注入使用。</p>
 */
@Configuration
@EnableConfigurationProperties(ApiLogProperties.class)
public class ApiLogAutoConfiguration {
}
