package com.platformcommons.common.mask;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 脱敏注解全局注册：为应用级 ObjectMapper 注入 {@link MaskAnnotationIntrospector}，
 * 使所有 Controller 响应中的 {@code @Mask} 字段序列化时自动脱敏。
 */
@Configuration
public class MaskAutoConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer maskCustomizer() {
        return builder -> builder.annotationIntrospector(new MaskAnnotationIntrospector());
    }
}
