package com.example.contact.domain.model;

import java.time.LocalDateTime;

/**
 * AuditLog domain entity.
 * Immutable after creation - records contact operations for audit purposes.
 */
public class AuditLog {

    private final Long id;
    private final Long contactId;
    private final LocalDateTime operationTime;
    private final OperationType operationType;
    private final String beforeData;
    private final String afterData;

    private AuditLog(Long id, Long contactId, LocalDateTime operationTime,
                     OperationType operationType, String beforeData, String afterData) {
        this.id = id;
        this.contactId = contactId;
        this.operationTime = operationTime;
        this.operationType = operationType;
        this.beforeData = beforeData;
        this.afterData = afterData;
    }

    /**
     * Factory method for creating a new audit log entry.
     */
    public static AuditLog create(Long contactId, OperationType operationType,
                                  String beforeData, String afterData) {
        return new AuditLog(
            null,
            contactId,
            LocalDateTime.now(),
            operationType,
            beforeData,
            afterData
        );
    }

    /**
     * Reconstruct AuditLog from persistence.
     */
    public static AuditLog reconstitute(Long id, Long contactId, LocalDateTime operationTime,
                                        OperationType operationType, String beforeData, String afterData) {
        return new AuditLog(id, contactId, operationTime, operationType, beforeData, afterData);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getContactId() {
        return contactId;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public String getAfterData() {
        return afterData;
    }
}
