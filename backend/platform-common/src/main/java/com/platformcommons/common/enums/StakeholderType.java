package com.platformcommons.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 利益相关方枚举。
 *
 * <p>对应宪章四类成员。{@link #getWeight()} 返回多院治理表决中的权重，
 * 劳动者权重高于其他类型，体现劳动优先原则。</p>
 */
@Getter
@AllArgsConstructor
public enum StakeholderType {

    WORKER("劳动者", 0.4),
    CONSUMER("消费者", 0.2),
    MERCHANT("商户", 0.2),
    PUBLIC_MEMBER("公共成员", 0.2);

    private final String description;
    private final double weight;
}
