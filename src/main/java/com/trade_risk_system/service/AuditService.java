package com.trade_risk_system.service;

import com.trade_risk_system.model.AuditLog;
import com.trade_risk_system.model.User;
import com.trade_risk_system.repository.AuditLogRepository;
import com.trade_risk_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing system audit logs.
 * Audit logs are persisted in a separate transaction to ensure traceability
 * even if the calling operation fails (depending on propagation).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Records a system action performed by a user.
     * 
     * @param action The name of the action being performed (e.g., TRADE_EXECUTED)
     * @param userId The ID of the user performing the action
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Could not log audit action: User not found with id {}", userId);
                return;
            }

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .user(user)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} for user {}", action, userId);
        } catch (Exception e) {
            // Silently fail to not interrupt main transaction
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }
}
