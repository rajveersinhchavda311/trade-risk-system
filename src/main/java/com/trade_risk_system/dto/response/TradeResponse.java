package com.trade_risk_system.dto.response;

import com.trade_risk_system.model.enums.TradeSide;
import com.trade_risk_system.model.enums.TradeStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
                Long id,
                Long userId,
                Long instrumentId,
                String symbol,
                Integer quantity,
                BigDecimal price,
                TradeSide side,
                TradeStatus status,
                LocalDateTime timestamp) {
}
