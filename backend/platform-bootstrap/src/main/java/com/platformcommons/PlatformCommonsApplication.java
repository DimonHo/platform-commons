package com.platformcommons;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 平台共同体 · 启动入口（第1章 第1-5条）
 *
 * <p>主类置于根包 {@code com.platformcommons}，ComponentScan / JPA Repository 扫描 /
 * EntityScan 均默认覆盖 {@code com.platformcommons.**}，无需额外注解。
 */
@SpringBootApplication
public class PlatformCommonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformCommonsApplication.class, args);
    }
}
