package com.platformcommons.ai.domain;

import lombok.Getter;

/**
 * AI 公共监督审议角色（第12章 第60-63条）
 * <p>
 * 多角色交叉审议，每个角色代表不同的公共利益视角，
 * 确保 AI 决策不偏向单一利益方。
 */
@Getter
public enum AiReviewRole {

    /** 公共利益代表 */
    PUBLIC_INTEREST("公共利益代表"),

    /** 劳动者权益代表 */
    WORKER_RIGHTS("劳动者权益代表"),

    /** 消费者权益代表 */
    CONSUMER_RIGHTS("消费者权益代表"),

    /** 弱势群体保护代表 */
    MINORITY_PROTECTION("弱势群体保护代表"),

    /** 资本审计代表 */
    CAPITAL_AUDIT("资本审计代表"),

    /** 反方论辩代表（魔鬼代言人） */
    COUNTER_ARGUMENT("反方论辩代表"),

    /** 事实核查代表 */
    FACT_CHECK("事实核查代表");

    private final String description;

    AiReviewRole(String description) {
        this.description = description;
    }
}
