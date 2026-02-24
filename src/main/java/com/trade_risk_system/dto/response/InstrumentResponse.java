package com.trade_risk_system.dto.response;

import java.math.BigDecimal;

public record InstrumentResponse(
                Long id,
                String symbol,
                String name,
                BigDecimal currentPrice) {
}
