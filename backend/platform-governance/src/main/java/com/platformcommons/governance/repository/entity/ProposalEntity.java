package com.platformcommons.governance.repository.entity;

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
 * 提案实体（JPA 持久化）。
 *
 * <p>阿里规范：POJO/Entity 必须重写 {@code toString()}；
 * 包装类型字段使用 {@code equals} 比较；表名使用下划线命名。</p>
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "proposal")
@EqualsAndHashCode(of = "id")
public class ProposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "proposer_id", nullable = false)
    private Long proposerId;

    @Column(name = "target_chamber", length = 32)
    private String targetChamber;

    @Column(name = "voting_start_at")
    private LocalDateTime votingStartAt;

    @Column(name = "voting_end_at")
    private LocalDateTime votingEndAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
