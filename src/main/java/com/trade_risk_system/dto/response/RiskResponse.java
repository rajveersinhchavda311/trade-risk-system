package com.trade_risk_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RiskResponse(
                Long portfolioId,
                BigDecimal totalExposure,
                BigDecimal concentrationRisk,
                BigDecimal riskScore,
                LocalDateTime timestamp) {
}
