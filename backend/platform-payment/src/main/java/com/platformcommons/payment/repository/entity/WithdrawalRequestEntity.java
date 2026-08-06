package com.platformcommons.payment.repository.entity;

import com.platformcommons.payment.domain.WithdrawalStatus;
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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 提现申请持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "withdrawal_request")
public class WithdrawalRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "request_no", nullable = false, length = 64)
    private String requestNo;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "bank_card_id", nullable = false)
    private Long bankCardId;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "fee", precision = 19, scale = 4)
    private BigDecimal fee;

    @Column(name = "status", length = 16)
    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "reject_reason", length = 256)
    private String rejectReason;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "completed_at")
    private Instant completedAt;
}
