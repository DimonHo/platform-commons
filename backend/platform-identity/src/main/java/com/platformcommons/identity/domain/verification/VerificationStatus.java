package com.platformcommons.identity.domain.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 核验状态枚举。
 */
@Getter
@AllArgsConstructor
public enum VerificationStatus {

    PENDING("待核验"),
    VERIFIED("已核验"),
    REJECTED("已驳回");

    private final String description;

}