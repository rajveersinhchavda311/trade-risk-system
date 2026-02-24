package com.trade_risk_system.service;

import com.trade_risk_system.dto.response.RiskResponse;
import com.trade_risk_system.exception.ResourceNotFoundException;
import com.trade_risk_system.model.Portfolio;
import com.trade_risk_system.model.Position;
import com.trade_risk_system.model.RiskMetric;
import com.trade_risk_system.repository.PortfolioRepository;
import com.trade_risk_system.repository.PositionRepository;
import com.trade_risk_system.repository.RiskMetricRepository;
import com.trade_risk_system.util.MoneyUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {

    private static final Logger log = LoggerFactory.getLogger(RiskService.class);

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final RiskMetricRepository riskMetricRepository;
    private final AuditService auditService;

    /**
     * Computes the real-time risk exposure for a portfolio.
     * Risk Metrics:
     * - Total Exposure: Sum of (quantity * currentPrice) for all held positions.
     * - Concentration Risk: (Value of largest position / Total Exposure).
     * - Risk Score: Concentration risk normalized to a 0-100 scale.
     * 
     * @param portfolioId The ID of the portfolio to analyze
     * @return RiskResponse containing analytical metrics
     * @throws ResourceNotFoundException if portfolio is not found
     */
    @Transactional
    @Cacheable(value = "risk", key = "#portfolioId")
    public RiskResponse calculatePortfolioRisk(Long portfolioId) {
        log.info("RISK_CALCULATION_START | Portfolio: {}", portfolioId);

        // A) Fetch Portfolio
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + portfolioId));

        // B) Fetch all Positions
        List<Position> positions = positionRepository.findByPortfolioIdWithInstrument(portfolioId);

        if (positions.isEmpty()) {
            RiskResponse emptyResponse = new RiskResponse(portfolioId, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, LocalDateTime.now());
            auditService.logAction("RISK_CALCULATED", portfolio.getUser().getId());
            return emptyResponse;
        }

        // C) Compute Metrics
        BigDecimal totalExposure = BigDecimal.ZERO;
        BigDecimal maxPositionValue = BigDecimal.ZERO;

        for (Position pos : positions) {
            BigDecimal currentPrice = pos.getInstrument().getCurrentPrice();
            BigDecimal priceToUse = (currentPrice != null) ? currentPrice : pos.getAvgPrice();
            BigDecimal posValue = priceToUse.multiply(BigDecimal.valueOf(pos.getQuantity()));

            totalExposure = totalExposure.add(posValue);
            if (posValue.compareTo(maxPositionValue) > 0) {
                maxPositionValue = posValue;
            }
        }

        BigDecimal concentrationRisk = (totalExposure.compareTo(BigDecimal.ZERO) > 0)
                ? maxPositionValue.divide(totalExposure, MoneyUtils.RATIO_SCALE, MoneyUtils.ROUNDING)
                : BigDecimal.ZERO;
        BigDecimal riskScore = MoneyUtils.scale(concentrationRisk.multiply(BigDecimal.valueOf(100)));

        // D) Persist RiskMetric snapshot
        RiskMetric snapshot = RiskMetric.builder()
                .portfolio(portfolio)
                .totalExposure(MoneyUtils.scale(totalExposure))
                .concentrationRisk(concentrationRisk)
                .riskScore(riskScore)
                .timestamp(LocalDateTime.now())
                .build();

        riskMetricRepository.save(snapshot);

        // E) Audit Integration
        auditService.logAction("RISK_CALCULATED", portfolio.getUser().getId());

        log.info("RISK_CALCULATION_SUCCESS | Portfolio: {} | Exposure: {} | Score: {}",
                portfolioId, totalExposure, riskScore);

        return mapToResponse(portfolioId, MoneyUtils.scale(totalExposure), concentrationRisk, riskScore,
                snapshot.getTimestamp());
    }

    public Page<RiskResponse> getRiskHistory(Long portfolioId, Pageable pageable) {
        return riskMetricRepository.findByPortfolioId(portfolioId, pageable)
                .map(m -> mapToResponse(m.getPortfolio().getId(), m.getTotalExposure(), m.getConcentrationRisk(),
                        m.getRiskScore(), m.getTimestamp()));
    }

    public Page<RiskResponse> getRiskHistoryByDate(Long portfolioId, LocalDateTime start, LocalDateTime end,
            Pageable pageable) {
        return riskMetricRepository.findByPortfolioIdAndTimestampBetween(portfolioId, start, end, pageable)
                .map(m -> mapToResponse(m.getPortfolio().getId(), m.getTotalExposure(), m.getConcentrationRisk(),
                        m.getRiskScore(), m.getTimestamp()));
    }

    private RiskResponse mapToResponse(Long portfolioId, BigDecimal totalExposure, BigDecimal concentrationRisk,
            BigDecimal riskScore, LocalDateTime timestamp) {
        return new RiskResponse(
                portfolioId,
                totalExposure,
                concentrationRisk,
                riskScore,
                timestamp);
    }
}
