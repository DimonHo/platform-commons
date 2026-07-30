package com.platformcommons.identity.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 成员实体（JPA 持久化）。
 *
 * <p>阿里规范：POJO/Entity 必须重写 {@code toString()}；
 * 包装类型字段使用 {@code equals} 比较；表名使用下划线命名。</p>
 */
@Entity
@Table(name = "member")
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

    public MemberEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Integer getLaborShares() {
        return laborShares;
    }

    public void setLaborShares(Integer laborShares) {
        this.laborShares = laborShares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemberEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MemberEntity{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", phone='" + phone + '\''
                + ", roles='" + roles + '\''
                + ", status='" + status + '\''
                + ", registeredAt=" + registeredAt
                + ", laborShares=" + laborShares
                + '}';
    }
}
