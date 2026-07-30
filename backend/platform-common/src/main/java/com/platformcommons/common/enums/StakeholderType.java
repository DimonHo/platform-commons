package com.platformcommons.common.enums;

/**
 * 利益相关方枚举。
 *
 * <p>对应宪章四类成员。{@link #getWeight()} 返回多院治理表决中的权重，
 * 劳动者权重高于其他类型，体现劳动优先原则。</p>
 */
public enum StakeholderType {

    WORKER("劳动者"),
    CONSUMER("消费者"),
    MERCHANT("商户"),
    PUBLIC_MEMBER("公共成员");

    /** 劳动者治理权重 */
    private static final double WEIGHT_WORKER = 0.4D;
    /** 消费者治理权重 */
    private static final double WEIGHT_CONSUMER = 0.2D;
    /** 商户治理权重 */
    private static final double WEIGHT_MERCHANT = 0.2D;
    /** 公共成员治理权重 */
    private static final double WEIGHT_PUBLIC = 0.2D;
    /** 未知类型兜底权重 */
    private static final double WEIGHT_UNKNOWN = 0.0D;

    private final String description;
    private final double weight;

    StakeholderType(String description) {
        this.description = description;
        this.weight = resolveWeight(this);
    }

    private static double resolveWeight(StakeholderType type) {
        return switch (type) {
            case WORKER -> WEIGHT_WORKER;
            case CONSUMER -> WEIGHT_CONSUMER;
            case MERCHANT -> WEIGHT_MERCHANT;
            case PUBLIC_MEMBER -> WEIGHT_PUBLIC;
        };
    }

    public String getDescription() {
        return description;
    }

    /**
     * 返回治理表决权重（0.0 ~ 1.0）。
     */
    public double getWeight() {
        return weight;
    }
}
