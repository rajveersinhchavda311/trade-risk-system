package com.trade_risk_system.dto.response;

import com.trade_risk_system.model.enums.Role;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        Role role) {
}
