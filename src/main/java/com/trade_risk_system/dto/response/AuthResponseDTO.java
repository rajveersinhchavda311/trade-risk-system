package com.trade_risk_system.dto.response;

import com.trade_risk_system.model.enums.Role;

public record AuthResponseDTO(
        String token,
        String username,
        Role role) {
}
