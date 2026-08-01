package com.platformcommons.common.thread;

import com.platformcommons.common.trace.TraceContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 全局 CPU 线程池——专用于 CPU 密集型任务。
 *
 * <p>池大小固定为 {@code Runtime.availableProcessors()}，全局唯一共享，
 * JVM 关闭时自动优雅停机（最长等待 10 秒）。</p>
 *
 * <p><b>选择指南</b>：</p>
 * <ul>
 *   <li><b>IO 密集型</b>（网络调用、数据库、文件读写）→ {@link VirtualThreads} 虚拟线程</li>
 *   <li><b>CPU 密集型</b>（加解密、压缩、序列化、大计算）→ 本类</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 1. 无返回值
 * PlatformThreads.runAsync(() -> compressImage(bytes));
 *
 * // 2. 有返回值
 * CompletableFuture<byte[]> future = PlatformThreads.supplyAsync(() -> encrypt(data));
 *
 * // 3. 批量并发，等待全部完成
 * List<CompletableFuture<Result>> futures = tasks.stream()
 *         .map(t -> PlatformThreads.supplyAsync(() -> process(t)))
 *         .toList();
 * futures.forEach(CompletableFuture::join);
 * }</pre>
 *
 * <h2>使用红线</h2>
 * <ul>
 *   <li>🚫 禁止在本池执行 IO 阻塞任务——会占满 CPU 线程，拖垮全局计算能力</li>
 *   <li>🚫 禁止自行创建 {@code Executors.newFixedThreadPool} 等——统一走本类</li>
 *   <li>🚫 禁止 {@code shutdown()} 本池——全局共享，由 JVM shutdown hook 管理</li>
 * </ul>
 *
 * @see VirtualThreads
 */
public final class PlatformThreads {

    /** CPU 核心数，决定线程池大小 */
    static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    /** 全局共享 CPU 线程池（固定大小 = CPU 核心数） */
    private static final ExecutorService CPU_POOL = createCpuPool();

    private PlatformThreads() {
    }

    private static ExecutorService createCpuPool() {
        ExecutorService pool = Executors.newFixedThreadPool(
                CPU_CORES,
                Thread.ofPlatform().name("platform-cpu-", 0).factory());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }, "platform-cpu-shutdown"));
        return pool;
    }

    /**
     * 异步执行 CPU 密集型任务（无返回值），自动传播 MDC 上下文。
     *
     * @param task 要执行的任务
     * @return CompletableFuture（可用于链式组合或等待完成）
     */
    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(TraceContext.wrap(task), CPU_POOL);
    }

    /**
     * 异步执行 CPU 密集型任务（有返回值），自动传播 MDC 上下文。
     *
     * @param task 要执行的任务
     * @param <T>  返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(TraceContext.wrap(task), CPU_POOL);
    }

    /**
     * 获取底层线程池实例（用于需要直接操作 Executor 的场景，如 Spring 集成）。
     *
     * @return 全局 CPU 线程池
     */
    public static ExecutorService getExecutor() {
        return CPU_POOL;
    }
}
