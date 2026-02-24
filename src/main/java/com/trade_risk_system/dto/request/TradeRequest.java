package com.trade_risk_system.dto.request;

import com.trade_risk_system.model.enums.TradeSide;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TradeRequest(
                @NotNull(message = "User ID is required") Long userId,

                @NotNull(message = "Instrument ID is required") Long instrumentId,

                @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") Integer quantity,

                @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal price,

                @NotNull(message = "Trade side is required") TradeSide side) {
}
