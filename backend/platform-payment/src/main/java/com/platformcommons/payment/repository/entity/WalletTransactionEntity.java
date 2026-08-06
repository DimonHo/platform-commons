package com.platformcommons.payment.repository.entity;

import com.platformcommons.payment.domain.TransactionDirection;
import com.platformcommons.payment.domain.WalletBusinessType;
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
 * 钱包流水持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "wallet_transaction")
public class WalletTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "transaction_no", nullable = false, length = 64)
    private String transactionNo;

    @Column(name = "direction", length = 8)
    @Enumerated(EnumType.STRING)
    private TransactionDirection direction;

    @Column(name = "amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "business_type", length = 32)
    @Enumerated(EnumType.STRING)
    private WalletBusinessType businessType;

    @Column(name = "ref_type", length = 32)
    private String refType;

    @Column(name = "ref_id", length = 64)
    private String refId;

    @Column(name = "remark", length = 256)
    private String remark;

    @Column(name = "created_at")
    private Instant createdAt;
}
