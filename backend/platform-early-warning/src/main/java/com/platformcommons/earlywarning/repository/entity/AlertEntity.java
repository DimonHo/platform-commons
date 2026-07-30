package com.platformcommons.earlywarning.repository.entity;

import com.platformcommons.earlywarning.domain.AlertCategory;
import com.platformcommons.earlywarning.domain.AlertLevel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 预警持久化实体。
 */
@Entity
@Table(name = "early_warning_alert")
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 16)
    private AlertLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private AlertCategory category;

    @Column(name = "red_line_code", length = 16)
    private String redLineCode;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "source_metric", length = 128)
    private String sourceMetric;

    @Column(name = "threshold", length = 128)
    private String threshold;

    @Column(name = "auto_measure_triggered", nullable = false)
    private boolean autoMeasureTriggered;

    @Column(name = "acknowledged", nullable = false)
    private boolean acknowledged;

    @Column(name = "acknowledged_by", length = 64)
    private String acknowledgedBy;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    @Column(name = "cleared_at")
    private Instant clearedAt;

    public AlertEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public AlertCategory getCategory() {
        return category;
    }

    public void setCategory(AlertCategory category) {
        this.category = category;
    }

    public String getRedLineCode() {
        return redLineCode;
    }

    public void setRedLineCode(String redLineCode) {
        this.redLineCode = redLineCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceMetric() {
        return sourceMetric;
    }

    public void setSourceMetric(String sourceMetric) {
        this.sourceMetric = sourceMetric;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public boolean isAutoMeasureTriggered() {
        return autoMeasureTriggered;
    }

    public void setAutoMeasureTriggered(boolean autoMeasureTriggered) {
        this.autoMeasureTriggered = autoMeasureTriggered;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Instant triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Instant getClearedAt() {
        return clearedAt;
    }

    public void setClearedAt(Instant clearedAt) {
        this.clearedAt = clearedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlertEntity that)) {
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
        return "AlertEntity{id=" + id
                + ", level=" + level
                + ", category=" + category
                + ", redLineCode='" + redLineCode + '\''
                + ", title='" + title + '\''
                + ", autoMeasureTriggered=" + autoMeasureTriggered
                + ", acknowledged=" + acknowledged
                + ", triggeredAt=" + triggeredAt + '}';
    }
}
