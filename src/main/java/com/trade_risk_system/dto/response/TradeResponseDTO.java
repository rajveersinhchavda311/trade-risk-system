package com.trade_risk_system.dto.response;

import com.trade_risk_system.model.enums.TradeSide;
import com.trade_risk_system.model.enums.TradeStatus;
import java.time.LocalDateTime;

public record TradeResponseDTO(
        Long id,
        Long userId,
        Long instrumentId,
        String symbol,
        Integer quantity,
        Double price,
        TradeSide side,
        TradeStatus status,
        LocalDateTime timestamp) {
}
