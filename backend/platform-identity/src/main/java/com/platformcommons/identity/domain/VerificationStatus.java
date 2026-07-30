package com.platformcommons.identity.domain;

/**
 * 核验状态枚举。
 */
public enum VerificationStatus {

    PENDING("待核验"),
    VERIFIED("已核验"),
    REJECTED("已驳回");

    private final String description;

    VerificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
