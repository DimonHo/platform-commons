package com.platformcommons.identity.domain;

import lombok.Getter;

/**
 * 核验方式枚举。
 */
@Getter
public enum VerificationType {

    PHONE("手机号核验"),
    ID_CARD("身份证核验"),
    BUSINESS_LICENSE("营业执照核验"),
    ORG_CERT("组织机构核验");

    private final String description;

    VerificationType(String description) {
        this.description = description;
    }
}
