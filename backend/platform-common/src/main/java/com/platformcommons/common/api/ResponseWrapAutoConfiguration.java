package com.platformcommons.common.api;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 响应包装相关 Bean 配置。
 *
 * <p>显式注册 {@link ResponseWrapProperties} 为 Spring Bean，
 * 供 {@link GlobalResponseAdvice} 注入使用。</p>
 */
@Configuration
@EnableConfigurationProperties(ResponseWrapProperties.class)
public class ResponseWrapAutoConfiguration {
}
