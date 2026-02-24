package com.trade_risk_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risk_metrics", indexes = {
        @jakarta.persistence.Index(name = "idx_risk_portfolio", columnList = "portfolio_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalExposure;

    @Column(precision = 19, scale = 8)
    private BigDecimal concentrationRisk;

    @Column(precision = 19, scale = 4)
    private BigDecimal riskScore;

    private LocalDateTime timestamp;
}
