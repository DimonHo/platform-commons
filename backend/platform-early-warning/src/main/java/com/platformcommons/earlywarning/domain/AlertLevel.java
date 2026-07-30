package com.platformcommons.earlywarning.domain;

/**
 * 预警等级枚举。
 *
 * <p>映射宪章第16章 99条：采用四色预警体系。
 */
public enum AlertLevel {

    /** 绿色：正常。 */
    GREEN,

    /** 黄色：关注。 */
    YELLOW,

    /** 橙色：警告。 */
    ORANGE,

    /** 红色：危险（触发红线）。 */
    RED
}
