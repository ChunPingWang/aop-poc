package com.example.contact.infrastructure.audit;

import com.example.contact.application.port.out.AuditLogRepository;
import com.example.contact.domain.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation of AuditEventHandler that persists audit events
 * to the database using AuditLogRepository.
 *
 * <p>This handler is the default choice for most applications and provides
 * reliable, transactional audit logging to the audit_logs table.</p>
 *
 * <p>To use a different handler (e.g., Kafka, external audit system),
 * mark this bean as @Primary(false) and create your own implementation.</p>
 */
@Component
public class DatabaseAuditEventHandler implements AuditEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseAuditEventHandler.class);

    private final AuditLogRepository auditLogRepository;

    public DatabaseAuditEventHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void handle(AuditContext context) {
        try {
            AuditLog auditLog = AuditLog.create(
                context.entityId(),
                context.operationType(),
                context.beforeData(),
                context.afterData()
            );
            auditLogRepository.save(auditLog);
            LOG.debug("Audit log persisted for {} operation on {} entity with ID {}",
                context.operationType(), context.entityType(), context.entityId());
        } catch (Exception e) {
            LOG.error("Failed to persist audit log for {} operation on {} entity with ID {}: {}",
                context.operationType(), context.entityType(), context.entityId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // Low priority - runs last
    }
}
