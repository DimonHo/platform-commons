package com.platformcommons.techgovernance.repository.entity;

import com.platformcommons.techgovernance.domain.VerificationStatus;
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

/**
 * 部署记录实体
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tech_deployment_records")
public class DeploymentRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String deploymentId;

    @Column(length = 128)
    private String commitHash;

    @Column(length = 128)
    private String buildArtifactHash;

    @Column(length = 128)
    private String configDigest;

    @Column(length = 64)
    private String deployedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VerificationStatus verificationStatus;
}
