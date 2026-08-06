package com.platformcommons.payment.domain.bankcard;

import com.platformcommons.payment.domain.bankcard.CardStatus;
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
 * 银行卡持久化实体。
 *
 * <p>cardType 字段为 String（DEBIT / CREDIT），不使用枚举。</p>
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "bank_card")
public class BankCardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "holder_name", length = 64)
    private String holderName;

    @Column(name = "card_no_enc", length = 256)
    private String cardNoEnc;

    @Column(name = "card_no_masked", length = 32)
    private String cardNoMasked;

    @Column(name = "bank_name", length = 64)
    private String bankName;

    @Column(name = "card_type", length = 16)
    private String cardType;

    @Column(name = "reserved_phone", length = 20)
    private String reservedPhone;

    @Column(name = "external_token", length = 128)
    private String externalToken;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "status", length = 16)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(name = "bound_at")
    private Instant boundAt;
}
