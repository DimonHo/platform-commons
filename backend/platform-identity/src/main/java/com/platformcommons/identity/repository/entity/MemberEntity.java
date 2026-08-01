package com.platformcommons.identity.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 成员实体（JPA 持久化）。
 *
 * <p>阿里规范：POJO/Entity 必须重写 {@code toString()}；
 * 包装类型字段使用 {@code equals} 比较；表名使用下划线命名。</p>
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "member")
@EqualsAndHashCode(of = "id")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    /** 角色集合，以逗号分隔存储，如 {@code WORKER,CONSUMER} */
    @Column(nullable = false, length = 64)
    private String roles;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "labor_shares")
    private Integer laborShares;
}
