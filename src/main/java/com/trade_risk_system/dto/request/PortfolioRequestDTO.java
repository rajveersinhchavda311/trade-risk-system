package com.trade_risk_system.dto.request;

import jakarta.validation.constraints.NotNull;

public record PortfolioRequestDTO(
        @NotNull(message = "User ID is required") Long userId) {
}
