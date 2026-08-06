package com.platformcommons.payment.domain.transaction;

import java.util.Objects;

import java.math.BigDecimal;

/**
 * 分账规则。
 *
 * <p>映射宪章第9章 47条：劳动者返还比例不得低于 70% 反榨取底线，平台服务费上限公开。
 * 本规则为静态常量，任何运行时变更必须通过新版本号发布并留痕。
 *
 * @param version              规则版本
 * @param platformFeeRate      平台服务费率（占订单总价）
 * @param minWorkerShareRate   劳动者最低返还比例（反榨取底线，不得低于 {@link #ANTI_EXPLOITATION_FLOOR}）
 * @param maxPlatformFeeRate   平台服务费上限
 * @param effectiveFrom        规则生效起始时间（epoch 毫秒）
 */
public record SettlementRule(
        String version,
        BigDecimal platformFeeRate,
        BigDecimal minWorkerShareRate,
        BigDecimal maxPlatformFeeRate,
        long effectiveFrom
) {

    /** 反榨取底线：劳动者返还不得低于订单可分配结余的 70%。 */
    public static final BigDecimal ANTI_EXPLOITATION_FLOOR = new BigDecimal("0.70");

    /** 平台服务费上限：8%。 */
    public static final BigDecimal PLATFORM_FEE_CEILING = new BigDecimal("0.08");

    /** 默认平台服务费率：5%。 */
    public static final BigDecimal DEFAULT_PLATFORM_FEE_RATE = new BigDecimal("0.05");

    /** 默认规则版本。 */
    public static final String DEFAULT_VERSION = "v1-2026";

    /** 默认规则生效起始时间：2026-01-01T00:00:00Z。 */
    public static final long DEFAULT_EFFECTIVE_FROM = 1767225600000L;

    /**
     * 当前生效的默认分账规则（静态常量）。
     */
    public static final SettlementRule DEFAULT = new SettlementRule(
            DEFAULT_VERSION,
            DEFAULT_PLATFORM_FEE_RATE,
            ANTI_EXPLOITATION_FLOOR,
            PLATFORM_FEE_CEILING,
            DEFAULT_EFFECTIVE_FROM
    );

    /**
     * 紧凑构造器：校验反榨取底线。
     *
     * @throws IllegalArgumentException 若劳动者返还比例低于反榨取底线
     */
    public SettlementRule {
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(minWorkerShareRate, "minWorkerShareRate must not be null");
        if (minWorkerShareRate.compareTo(ANTI_EXPLOITATION_FLOOR) < 0) {
            throw new IllegalArgumentException(
                    "minWorkerShareRate must be >= ANTI_EXPLOITATION_FLOOR (70%): " + minWorkerShareRate);
        }
        if (platformFeeRate != null && maxPlatformFeeRate != null
                && platformFeeRate.compareTo(maxPlatformFeeRate) > 0) {
            throw new IllegalArgumentException(
                    "platformFeeRate must be <= maxPlatformFeeRate: " + platformFeeRate);
        }
    }
}
