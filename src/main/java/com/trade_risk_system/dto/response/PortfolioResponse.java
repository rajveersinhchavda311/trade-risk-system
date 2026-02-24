package com.trade_risk_system.dto.response;

import java.math.BigDecimal;

public record PortfolioResponse(
                Long id,
                Long userId,
                String username,
                BigDecimal totalValue) {
}
