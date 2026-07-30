package com.platformcommons.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 平台共同体 · 启动入口（第1章 第1-5条）
 * <p>
 * 所有模块通过 ComponentScan 自动扫描，
 * Spring Boot 4.1.0 统一管理 Bean 生命周期。
 *
 * <pre>
 * 扫描包路径：com.platformcommons.**
 * 包含模块：
 *   - platform-common      共享基础
 *   - platform-governance   治理
 *   - platform-identity     身份
 *   - platform-payment      支付
 *   - platform-mutual       互助
 *   - platform-matching     匹配
 *   - platform-ai-supervision  AI 监督
 *   - platform-tech-governance 技术治理
 *   - platform-finance      财务
 *   - platform-dispute      争议
 * </pre>
 */
@SpringBootApplication(scanBasePackages = "com.platformcommons")
public class PlatformCommonsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformCommonsApplication.class, args);
    }
}
