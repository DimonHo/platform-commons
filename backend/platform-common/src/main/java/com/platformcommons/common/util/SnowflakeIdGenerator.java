package com.platformcommons.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 雪花式 ID 生成器。
 *
 * <p>格式（固定 32 位字符）：</p>
 * <pre>
 * | 时间戳                    | IP 尾段  | 序列号   |
 * | yyyyMMddHHmmssSSS (17)   | IP后5位  | 10位序列  |
 * | 共 17 + 5 + 10 = 32 位
 * </pre>
 *
 * <ul>
 *   <li><b>时间戳</b>（17 位）：{@code yyyyMMddHHmmssSSS}，精确到毫秒</li>
 *   <li><b>IP 尾段</b>（5 位）：本机 IPv4 去掉点号后取末 5 位，不足前补 0</li>
 *   <li><b>序列号</b>（10 位）：同一毫秒内从 1 自增，最大 9999999999，溢出自旋等待下一毫秒</li>
 * </ul>
 *
 * <p>线程安全，单机每毫秒可生成 10,000,000,000 个 ID。</p>
 */
public final class SnowflakeIdGenerator {

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

    private SnowflakeIdGenerator() {
    }

    /**
     * 生成 32 位雪花 ID。
     *
     * @return 形如 {@code 20260731120000000001270000000001} 的 32 位字符串
     */
    public static String nextId() {
        String timestamp;
        int seq;
        synchronized (SnowflakeIdGenerator.class) {
            timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            if (timestamp.equals(lastTimestamp)) {
                seq = SEQUENCE.incrementAndGet();
            } else {
                // 新毫秒：序列重置为 1
                lastTimestamp = timestamp;
                SEQUENCE.set(1);
                seq = 1;
            }
        }
        return timestamp + IP_SUFFIX + String.format("%010d", seq);
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
        String digits = raw.replace(".", "");
        if (digits.length() <= 5) {
            return String.format("%5s", digits).replace(' ', '0');
        }
        return digits.substring(digits.length() - 5);
    }
}
