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

import java.util.Objects;

/**
 * 部署记录实体
 */
@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getBuildArtifactHash() {
        return buildArtifactHash;
    }

    public void setBuildArtifactHash(String buildArtifactHash) {
        this.buildArtifactHash = buildArtifactHash;
    }

    public String getConfigDigest() {
        return configDigest;
    }

    public void setConfigDigest(String configDigest) {
        this.configDigest = configDigest;
    }

    public String getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    @Override
    public String toString() {
        return "DeploymentRecordEntity{"
                + "id=" + id
                + ", deploymentId='" + deploymentId + '\''
                + ", commitHash='" + commitHash + '\''
                + ", verificationStatus=" + verificationStatus
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeploymentRecordEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
