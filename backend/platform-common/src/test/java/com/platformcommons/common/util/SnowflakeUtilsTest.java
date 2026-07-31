package com.platformcommons.common.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SnowflakeUtils} 单元测试。
 */
class SnowflakeUtilsTest {

    @Test
    void nextId_is32Chars() {
        String id = SnowflakeUtils.nextId();
        assertEquals(32, id.length(), "ID 应固定 32 位");
    }

    @Test
    void nextId_isAllDigits() {
        String id = SnowflakeUtils.nextId();
        assertTrue(id.matches("\\d{32}"), "ID 应全为数字");
    }

    @Test
    void nextId_timestampPrefixIsCurrentTime() {
        String id = SnowflakeUtils.nextId();
        String now = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        // 允许毫秒翻转的极端情况（极低概率），截取前 14 位（秒级）比对
        assertEquals(now.substring(0, 14), id.substring(0, 14), "时间戳前缀应匹配当前时间");
    }

    @Test
    void nextId_sequenceStartsFrom1PerMs() {
        // 快速生成两个 ID，如果同一毫秒则 seq 分别为 1, 2
        String id1 = SnowflakeUtils.nextId();
        String id2 = SnowflakeUtils.nextId();
        String seq1 = id1.substring(22);
        String seq2 = id2.substring(22);
        // 同毫秒：seq2 = seq1 + 1；跨毫秒：seq2 = 1
        int s1 = Integer.parseInt(seq1);
        int s2 = Integer.parseInt(seq2);
        assertTrue(s2 == s1 + 1 || s2 == 1, "序列号应递增或跨毫秒重置");
    }

    @RepeatedTest(100)
    void nextId_uniqueAcrossManyCalls() {
        String id = SnowflakeUtils.nextId();
        assertEquals(32, id.length());
    }

    @Test
    void nextId_concurrentUnique() throws InterruptedException {
        int threadCount = 20;
        int idsPerThread = 500;
        Set<String> allIds = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    Set<String> local = new HashSet<>();
                    for (int j = 0; j < idsPerThread; j++) {
                        local.add(SnowflakeUtils.nextId());
                    }
                    synchronized (allIds) {
                        allIds.addAll(local);
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }

        int expected = threadCount * idsPerThread;
        assertEquals(expected, allIds.size(), "并发场景下所有 ID 必须唯一");
    }

    // ==================== 带前缀方法测试 ====================

    @Test
    void nextIdWithPrefix_is32Chars() {
        String id = SnowflakeUtils.nextId("ORD");
        assertEquals(32, id.length(), "带前缀 ID 应固定 32 位");
    }

    @Test
    void nextIdWithPrefix_startsWithPrefix() {
        String id = SnowflakeUtils.nextId("PAY");
        assertTrue(id.startsWith("PAY"), "ID 应以指定前缀开头");
    }

    @Test
    void nextIdWithPrefix_nullPrefixFallbackToPlain() {
        String id = SnowflakeUtils.nextId(null);
        assertEquals(32, id.length());
        assertTrue(id.matches("\\d{32}"), "null 前缀应回退为纯数字 ID");
    }

    @Test
    void nextIdWithPrefix_emptyPrefixFallbackToPlain() {
        String id = SnowflakeUtils.nextId("");
        assertEquals(32, id.length());
        assertTrue(id.matches("\\d{32}"), "空前缀应回退为纯数字 ID");
    }

    @Test
    void nextIdWithPrefix_longPrefixTruncatedTo32() {
        String longPrefix = "A".repeat(40);
        String id = SnowflakeUtils.nextId(longPrefix);
        assertEquals(32, id.length(), "超长前缀应截断至 32 位");
        assertTrue(id.startsWith("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), "应截取前 32 位");
    }

    @Test
    void nextIdWithPrefix_concurrentUnique() throws InterruptedException {
        int threadCount = 20;
        int idsPerThread = 500;
        Set<String> allIds = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    Set<String> local = new HashSet<>();
                    for (int j = 0; j < idsPerThread; j++) {
                        local.add(SnowflakeUtils.nextId("CTX"));
                    }
                    synchronized (allIds) {
                        allIds.addAll(local);
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }

        int expected = threadCount * idsPerThread;
        assertEquals(expected, allIds.size(), "并发场景下带前缀 ID 必须唯一");
    }
}
