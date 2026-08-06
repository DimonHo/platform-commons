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

import java.time.Instant;

/**
 * 实名认证实体（identity_verification 表）。
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "identity_verification")
@EqualsAndHashCode(of = "id")
public class IdentityVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(name = "id_card_type", length = 16)
    private String idCardType;

    @Column(name = "id_card_no_enc", length = 256)
    private String idCardNoEnc;

    @Column(name = "id_card_no_masked", length = 32)
    private String idCardNoMasked;

    @Column(length = 16)
    private String status;

    @Column(name = "verification_channel", length = 32)
    private String verificationChannel;

    @Column(name = "face_verified")
    private Boolean faceVerified;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "reviewer_id")
    private Long reviewerId;
}
