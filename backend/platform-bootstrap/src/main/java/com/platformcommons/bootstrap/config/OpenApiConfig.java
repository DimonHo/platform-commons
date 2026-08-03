package com.platformcommons.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.1 文档配置。
 *
 * <p>访问地址：
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("公地引擎 API")
                        .description("平台共同体 · 全模块接口文档")
                        .version("0.1.0")
                        .contact(new Contact().name("敖癸").email("vampirehgg@qq.com"))
                        .license(new License().name("MIT")));
    }
}
