package com.platformcommons.mutual.domain;

/**
 * 理赔申请状态枚举。
 *
 * <p>阿里规范：枚举值全大写下划线命名。
 */
public enum ClaimStatus {

    /** 待处理。 */
    PENDING,

    /** 调查中（反欺诈/资格认定）。 */
    INVESTIGATING,

    /** 已批准。 */
    APPROVED,

    /** 已拒绝。 */
    REJECTED
}
