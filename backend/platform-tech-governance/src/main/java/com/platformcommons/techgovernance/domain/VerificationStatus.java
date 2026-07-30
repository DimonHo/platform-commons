package com.platformcommons.techgovernance.domain;

/**
 * 核验状态（第13章 第71条）
 * <p>
 * 部署/算法规格核验的结论。
 */
public enum VerificationStatus {

    /** 已核验：代码与规格一致 */
    VERIFIED("已核验"),

    /** 不匹配：检测到不一致 */
    MISMATCH("不匹配"),

    /** 不可核验：缺乏足够信息进行核验 */
    UNVERIFIABLE("不可核验");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
