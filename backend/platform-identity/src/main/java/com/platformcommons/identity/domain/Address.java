package com.platformcommons.identity.domain;

import java.time.Instant;

/**
 * 收货地址领域模型（不可变 record）。
 *
 * @param id           主键
 * @param memberId     成员 ID
 * @param label        地址标签（如「家」「公司」）
 * @param receiverName 收件人姓名
 * @param phone        联系电话
 * @param province     省份
 * @param city         城市
 * @param district     区县
 * @param detail       详细地址
 * @param latitude     纬度
 * @param longitude    经度
 * @param isDefault    是否默认地址
 * @param createdAt    创建时间
 * @param updatedAt    更新时间
 */
public record Address(
        Long id,
        Long memberId,
        String label,
        String receiverName,
        String phone,
        String province,
        String city,
        String district,
        String detail,
        Double latitude,
        Double longitude,
        Boolean isDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
