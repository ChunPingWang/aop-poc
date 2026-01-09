package com.example.contact.infrastructure.event;

import com.example.contact.application.port.out.AuditLogRepository;
import com.example.contact.domain.event.ContactCreatedEvent;
import com.example.contact.domain.event.ContactDeletedEvent;
import com.example.contact.domain.event.ContactUpdatedEvent;
import com.example.contact.domain.model.AuditLog;
import com.example.contact.domain.model.OperationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that creates audit logs in response to domain events.
 *
 * <p>Uses {@link TransactionalEventListener} to ensure audit logs are only
 * created after the main transaction commits successfully.</p>
 *
 * <p>This approach completely decouples audit logging from business logic.
 * The service layer doesn't know about audit logging - it just publishes
 * domain events describing what happened.</p>
 */
@Component
public class AuditEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditEventListener(AuditLogRepository auditLogRepository,
                              ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactCreated(ContactCreatedEvent event) {
        LOG.debug("Handling ContactCreatedEvent for contact ID: {}", event.getContactId());

        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.CREATE,
            null,
            toJson(event.getSnapshot())
        );
        auditLogRepository.save(auditLog);

        LOG.info("Audit log created for CREATE operation on contact {}", event.getContactId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactUpdated(ContactUpdatedEvent event) {
        LOG.debug("Handling ContactUpdatedEvent for contact ID: {}", event.getContactId());

        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.UPDATE,
            toJson(event.getBeforeSnapshot()),
            toJson(event.getSnapshot())
        );
        auditLogRepository.save(auditLog);

        LOG.info("Audit log created for UPDATE operation on contact {}", event.getContactId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactDeleted(ContactDeletedEvent event) {
        LOG.debug("Handling ContactDeletedEvent for contact ID: {}", event.getContactId());

        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.DELETE,
            toJson(event.getSnapshot()),
            null
        );
        auditLogRepository.save(auditLog);

        LOG.info("Audit log created for DELETE operation on contact {}", event.getContactId());
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
}
