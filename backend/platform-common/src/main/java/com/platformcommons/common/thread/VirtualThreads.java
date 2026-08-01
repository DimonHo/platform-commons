package com.platformcommons.common.thread;

import com.platformcommons.common.trace.TraceContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * 虚拟线程统一入口工具。
 *
 * <p>本类封装了项目内所有手动创建虚拟线程的场景，确保：</p>
 * <ol>
 *   <li><b>MDC 链路追踪自动传播</b>——所有方法内部自动调用 {@link TraceContext#wrap}，
 *       调用方无需手动包装。</li>
 *   <li><b>禁止裸用 {@code Thread.startVirtualThread} / {@code Executors.newVirtualThreadPerTaskExecutor}</b>
 *       ——统一从此处调用，便于未来全局管控（监控、限流、命名）。</li>
 *   <li><b>ExecutorService 必须关闭</b>——{@code newExecutor()} 返回的池需要在 try-with-resources 中使用。</li>
 * </ol>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 1. fire-and-forget（无返回值，自动传播 traceId）
 * VirtualThreads.runAsync(() -> sendNotification(userId));
 *
 * // 2. 需要返回值
 * CompletableFuture<String> future = VirtualThreads.supplyAsync(() -> queryExternalApi());
 *
 * // 3. 批量并发（ExecutorService 必须关闭）
 * try (ExecutorService pool = VirtualThreads.newExecutor("batch-import")) {
 *     for (var item : items) {
 *         pool.submit(() -> processItem(item));
 *     }
 * }
 * }</pre>
 *
 * <h2>使用红线</h2>
 * <ul>
 *   <li>🚫 禁止 {@code new Thread()} 创建平台线程</li>
 *   <li>🚫 禁止 {@code synchronized} 包裹可能阻塞 IO 的代码块——使用 {@code ReentrantLock}</li>
 *   <li>🚫 禁止 {@code ThreadLocal} + 虚拟线程场景——虚拟线程数量巨大，ThreadLocal 内存膨胀</li>
 *   <li>🚫 禁止池化虚拟线程（一个任务一个虚拟线程，用完即弃）</li>
 *   <li>✅ CPU 密集型任务仍用平台线程池（{@code ForkJoinPool} / 固定大小线程池）</li>
 * </ul>
 *
 * @see TraceContext
 */
public final class VirtualThreads {

    private VirtualThreads() {
    }

    /**
     * 异步执行无返回值任务（fire-and-forget），自动传播 MDC 上下文。
     *
     * @param task 要执行的任务
     * @return 代表该任务的 Thread 引用（通常可忽略）
     */
    public static Thread runAsync(Runnable task) {
        return Thread.startVirtualThread(TraceContext.wrap(task));
    }

    /**
     * 异步执行有返回值任务，返回 CompletableFuture，自动传播 MDC 上下文。
     *
     * @param task 要执行的任务
     * @param <T>  返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(TraceContext.wrap(task));
    }

    /**
     * 创建带命名的虚拟线程 ExecutorService。
     *
     * <p><b>必须在 try-with-resources 中使用</b>，确保任务结束后正确关闭。</p>
     *
     * @param name 线程名前缀（用于调试 / 线程 dump 定位）
     * @return 虚拟线程 ExecutorService
     */
    public static ExecutorService newExecutor(String name) {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name(name + "-", 0).factory());
    }

    /**
     * 提交有返回值任务到虚拟线程 ExecutorService，自动传播 MDC 上下文。
     *
     * @param pool 虚拟线程执行器
     * @param task 要执行的任务
     * @param <T>  返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> submit(ExecutorService pool, Supplier<T> task) {
        return CompletableFuture.supplyAsync(TraceContext.wrap(task), pool);
    }
}
