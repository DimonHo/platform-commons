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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 预警持久化实体。
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
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
}
