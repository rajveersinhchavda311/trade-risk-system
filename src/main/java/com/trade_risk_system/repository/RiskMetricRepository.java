package com.trade_risk_system.repository;

import com.trade_risk_system.model.RiskMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RiskMetricRepository extends JpaRepository<RiskMetric, Long> {
        Page<RiskMetric> findByPortfolioId(Long portfolioId, Pageable pageable);

        Page<RiskMetric> findByPortfolioIdAndTimestampBetween(
                        Long portfolioId,
                        LocalDateTime start,
                        LocalDateTime end,
                        Pageable pageable);
}
