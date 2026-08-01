package com.platformcommons.common.trace;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MDC 上下文传播装饰器。
 *
 * <p>Spring Boot 虚拟线程 / {@code @Async} 执行器会自动应用此装饰器，
 * 将父线程的 MDC 快照复制到子线程，保证异步任务中的日志也能关联 traceId。</p>
 *
 * <p>无需手动调用——注册为 Spring Bean 后，Spring 自动发现并注入到
 * {@code SimpleAsyncTaskExecutor} / {@code ThreadPoolTaskExecutor}。</p>
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        return () -> {
            if (snapshot != null) {
                MDC.setContextMap(snapshot);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
