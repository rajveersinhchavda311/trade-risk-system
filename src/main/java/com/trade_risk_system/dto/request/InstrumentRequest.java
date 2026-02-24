package com.trade_risk_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InstrumentRequest(
                @NotBlank(message = "Symbol is required") String symbol,

                @NotBlank(message = "Name is required") String name,

                @NotNull(message = "Current price is required") @Positive(message = "Current price must be positive") BigDecimal currentPrice) {
}
