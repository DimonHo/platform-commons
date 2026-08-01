package com.platformcommons.common.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TraceContext} 单元测试。
 *
 * <p>验证 MDC 上下文的设置 / 清除 / 跨线程传播。</p>
 */
class TraceContextTest {

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void setAndGetTraceId() {
        TraceContext.setTraceId("trace-123");
        assertEquals("trace-123", TraceContext.getTraceId());
    }

    @Test
    void clearRemovesTraceId() {
        TraceContext.setTraceId("trace-456");
        TraceContext.clear();
        assertNull(TraceContext.getTraceId());
    }

    @Test
    void wrapRunnablePropagatesMdcToVirtualThread() throws Exception {
        TraceContext.setTraceId("parent-trace");

        AtomicReference<String> childTrace = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread.startVirtualThread(TraceContext.wrap(() -> {
            childTrace.set(TraceContext.getTraceId());
            latch.countDown();
        }));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("parent-trace", childTrace.get(), "子线程应继承父线程 traceId");
    }

    @Test
    void wrapCallablePropagatesMdcToVirtualThread() throws Exception {
        TraceContext.setTraceId("call-trace");

        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        try {
            String childTrace = pool.submit(TraceContext.wrap((Callable<String>) () -> TraceContext.getTraceId())).get(2, TimeUnit.SECONDS);
            assertEquals("call-trace", childTrace, "Callable 子线程应继承父线程 traceId");
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void wrapRestoresChildMdcAfterExecution() throws Exception {
        TraceContext.setTraceId("parent");

        AtomicReference<String> afterTrace = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Thread.startVirtualThread(TraceContext.wrap(() -> {
            // 子线程中设置了自己的 MDC
            MDC.put(TraceContext.TRACE_ID_KEY, "child-temp");
            afterTrace.set(TraceContext.getTraceId());
            latch.countDown();
        }));

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        // 父线程 MDC 不应被修改
        assertEquals("parent", TraceContext.getTraceId(), "父线程 MDC 不应被子线程影响");
    }

    @Test
    void wrapCallableWithNoMdcDoesNotCrash() throws Exception {
        // 不设置任何 MDC
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
        try {
            String result = pool.submit(TraceContext.wrap((Callable<String>) () -> "ok")).get(2, TimeUnit.SECONDS);
            assertEquals("ok", result);
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void completableFuturePropagatesMdcViaWrap() throws Exception {
        TraceContext.setTraceId("cf-trace");

        AtomicReference<String> asyncTrace = new AtomicReference<>();
        CompletableFuture.runAsync(TraceContext.wrap(() -> {
            asyncTrace.set(TraceContext.getTraceId());
        })).get(2, TimeUnit.SECONDS);

        assertEquals("cf-trace", asyncTrace.get(), "CompletableFuture 应通过 wrap 传播 traceId");
    }

    @Test
    void multipleConcurrentVirtualThreadsEachInheritTraceId() throws Exception {
        TraceContext.setTraceId("concurrent-trace");

        int n = 50;
        CountDownLatch latch = new CountDownLatch(n);
        ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();

        try {
            for (int i = 0; i < n; i++) {
                pool.submit(TraceContext.wrap(() -> {
                    assertEquals("concurrent-trace", TraceContext.getTraceId());
                    latch.countDown();
                }));
            }
            assertTrue(latch.await(5, TimeUnit.SECONDS), "所有虚拟线程应在超时前完成");
        } finally {
            pool.shutdown();
        }
    }
}
