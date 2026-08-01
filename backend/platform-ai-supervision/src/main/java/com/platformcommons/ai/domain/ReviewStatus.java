package com.platformcommons.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审议状态（第12章 第67条）
 */
@AllArgsConstructor
@Getter
public enum ReviewStatus {

    /** 待审议 */
    PENDING("待审议"),

    /** 审议中 */
    IN_PROGRESS("审议中"),

    /** 审议完成 */
    COMPLETED("审议完成"),

    /** 存在争议，需升级 */
    CONTESTED("存在争议");

    private final String description;

}