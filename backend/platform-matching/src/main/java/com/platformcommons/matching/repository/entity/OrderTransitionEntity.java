package com.platformcommons.matching.repository.entity;

import com.platformcommons.matching.domain.OperatorRole;
import com.platformcommons.matching.domain.TransitionAction;
import com.platformcommons.matching.domain.WorkOrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 工单状态流转记录持久化实体，映射 {@code order_transition} 表。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "order_transition")
@EqualsAndHashCode(of = "id")
public class OrderTransitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private WorkOrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 16)
    private WorkOrderStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private TransitionAction action;

    @Column(name = "operator_id")
    private Long operatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator_role", length = 32)
    private OperatorRole operatorRole;

    @Column(name = "remark", length = 256)
    private String remark;

    @Column(name = "attachment_urls", length = 2048)
    private String attachmentUrls;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
