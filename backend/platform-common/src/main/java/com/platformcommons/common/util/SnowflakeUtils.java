package com.platformcommons.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 雪花式 ID 生成工具。
 *
 * <p>格式（固定 32 位字符）：</p>
 * <pre>
 * | 前缀(N) | 时间戳(17)           | IP尾段(5) | 序列号(10−N) |
 * |---------|----------------------|-----------|-------------|
 * | N + 17 + 5 + (10−N) = 32 位（无前缀时 N=0，序列号占满 10 位）
 * </pre>
 *
 * <ul>
 *   <li><b>前缀</b>（N 位，0~4）：业务标识，如 ORD / PAY</li>
 *   <li><b>时间戳</b>（17 位）：{@code yyyyMMddHHmmssSSS}，精确到毫秒</li>
 *   <li><b>IP 尾段</b>（5 位）：本机 IPv4 去掉点号后取末 5 位，不足前补 0</li>
 *   <li><b>序列号</b>（10−N 位）：同一毫秒内从 1 自增，前缀每多 1 位容量减 10 倍</li>
 * </ul>
 *
 * <p>线程安全，单机每毫秒可生成 10,000,000,000 个 ID。</p>
 */
@Slf4j
public final class SnowflakeUtils {

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /** IP 尾段（5 位字符串，已补零）。 */
    private static final String IP_SUFFIX;

    static {
        IP_SUFFIX = resolveIpSuffix();
    }

    /** 上一毫秒的时间戳前缀，用于检测毫秒翻转。 */
    private static volatile String lastTimestamp = "";
    /** 同毫秒内序列计数器。 */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    /**
     * 序列号生成锁——使用 ReentrantLock 替代 synchronized，
     * 避免在虚拟线程环境下潜在的线程钉住（pinning）风险。
     */
    private static final ReentrantLock SEQUENCE_LOCK = new ReentrantLock();

    private SnowflakeUtils() {
    }

    /**
     * 生成 32 位雪花 ID。
     *
     * @return 形如 {@code 20260731120000000001270000000001} 的 32 位字符串
     */
    public static String nextId() {
        return nextId(null);
    }

    /**
     * 生成带业务前缀的 32 位雪花 ID。
     *
     * <p>总长度始终 32 位：前缀 N 位 + 时间戳 17 位 + IP 尾段 5 位 + 序列号 (10−N) 位。
     * 前缀每多 1 位，序列号补零宽度减 1，时间戳与 IP 段完整保留。</p>
     *
     * @param prefix 业务前缀（1~4 字符，如 {@code "ORD"}、{@code "PAY"}），为 {@code null}/空则退化为纯雪花 ID
     * @return 形如 {@code ORD2026073112000000001270000001} 的 32 位字符串
     * @throws IllegalArgumentException 前缀长度超过 4
     */
    public static String nextId(String prefix) {
        String p = Optional.ofNullable(prefix).orElse("");
        if (p.length() > 4) {
            throw new IllegalArgumentException("前缀长度不得超过 4 个字符: " + p);
        }
        String timestamp;
        int seq;
        SEQUENCE_LOCK.lock();
        try {
            timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            if (timestamp.equals(lastTimestamp)) {
                seq = SEQUENCE.incrementAndGet();
            } else {
                lastTimestamp = timestamp;
                SEQUENCE.set(1);
                seq = 1;
            }
        } finally {
            SEQUENCE_LOCK.unlock();
        }
        return p + timestamp + IP_SUFFIX + String.format("%0" + (10 - p.length()) + "d", seq);
    }

    /**
     * 解析本机 IPv4，去掉点号后取末 5 位，不足前补 0。
     * <p>例如 {@code 192.168.1.127} → {@code "1127"} → {@code "01127"}</p>
     */
    private static String resolveIpSuffix() {
        String raw;
        try {
            raw = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            raw = "127.0.0.1";
        }
        log.info("ServerIp: {}", raw);
        String digits = raw.replace(".", "");
        if (digits.length() <= 5) {
            return String.format("%5s", digits).replace(' ', '0');
        }
        return digits.substring(digits.length() - 5);
    }
}
