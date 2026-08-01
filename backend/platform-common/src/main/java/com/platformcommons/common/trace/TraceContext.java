package com.platformcommons.common.trace;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 链路追踪上下文工具。
 *
 * <p>基于 SLF4J {@link MDC} 管理当前线程的 traceId，并提供 {@link #wrap(Runnable)} / {@link #wrap(Callable)}
 * 包装器，在虚拟线程、{@code CompletableFuture}、{@code @Async} 等跨线程场景下自动继承父线程的 MDC 上下文。</p>
 *
 * <p><b>使用示例</b>：</p>
 * <pre>{@code
 * // 手动创建虚拟线程时
 * Thread.startVirtualThread(TraceContext.wrap(() -> doWork()));
 *
 * // CompletableFuture（无返回值）
 * CompletableFuture.runAsync(TraceContext.wrap(() -> doWork()));
 *
 * // CompletableFuture（有返回值）
 * CompletableFuture.supplyAsync(TraceContext.wrap(() -> computeResult()));
 * }</pre>
 */
public final class TraceContext {

    /** MDC 中 traceId 的键名。 */
    public static final String TRACE_ID_KEY = "traceId";

    private TraceContext() {
    }

    /**
     * 获取当前线程的 traceId。
     *
     * @return traceId，未设置则返回 {@code null}
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 设置当前线程的 traceId。
     *
     * @param traceId 链路追踪 ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 清除当前线程的 traceId。
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 包装 Runnable，使其在子线程中继承当前线程的 MDC 上下文。
     *
     * @param task 原始任务
     * @return 包装后的 Runnable
     */
    public static Runnable wrap(Runnable task) {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (snapshot != null) {
                MDC.setContextMap(snapshot);
            }
            try {
                task.run();
            } finally {
                restoreMdc(previous);
            }
        };
    }

    /**
     * 包装 Callable，使其在子线程中继承当前线程的 MDC 上下文。
     *
     * @param task 原始任务
     * @return 包装后的 Callable
     * @param <T> 返回类型
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (snapshot != null) {
                MDC.setContextMap(snapshot);
            }
            try {
                return task.call();
            } finally {
                restoreMdc(previous);
            }
        };
    }

    /**
     * 包装 Supplier，使其在子线程中继承当前线程的 MDC 上下文。
     * 用于 {@code CompletableFuture.supplyAsync()} 等需要返回值的异步场景。
     *
     * @param task 原始任务
     * @param <T> 返回类型
     * @return 包装后的 Supplier
     */
    public static <T> Supplier<T> wrap(Supplier<T> task) {
        Map<String, String> snapshot = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (snapshot != null) {
                MDC.setContextMap(snapshot);
            }
            try {
                return task.get();
            } finally {
                restoreMdc(previous);
            }
        };
    }

    /**
     * 恢复 MDC 到先前状态。
     *
     * @param previous 先前的 MDC 快照，为 null 则清空
     */
    private static void restoreMdc(Map<String, String> previous) {
        if (previous != null) {
            MDC.setContextMap(previous);
        } else {
            MDC.clear();
        }
    }
}
