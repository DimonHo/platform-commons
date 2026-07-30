package com.platformcommons.finance.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 融资记录实体
 */
@Entity
@Table(name = "finance_financing_records")
public class FinancingRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String recordId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 64)
    private String financingType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal repaymentCap;

    @Column(nullable = false)
    private Boolean noGovernance;

    @Column(length = 32)
    private String disclosedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFinancingType() {
        return financingType;
    }

    public void setFinancingType(String financingType) {
        this.financingType = financingType;
    }

    public BigDecimal getRepaymentCap() {
        return repaymentCap;
    }

    public void setRepaymentCap(BigDecimal repaymentCap) {
        this.repaymentCap = repaymentCap;
    }

    public Boolean getNoGovernance() {
        return noGovernance;
    }

    public void setNoGovernance(Boolean noGovernance) {
        this.noGovernance = noGovernance;
    }

    public String getDisclosedAt() {
        return disclosedAt;
    }

    public void setDisclosedAt(String disclosedAt) {
        this.disclosedAt = disclosedAt;
    }

    @Override
    public String toString() {
        return "FinancingRecordEntity{"
                + "id=" + id
                + ", recordId='" + recordId + '\''
                + ", amount=" + amount
                + ", financingType='" + financingType + '\''
                + ", repaymentCap=" + repaymentCap
                + ", noGovernance=" + noGovernance
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FinancingRecordEntity that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
