package com.example.contact.infrastructure.audit;

import com.example.contact.domain.model.OperationType;

import java.time.LocalDateTime;

/**
 * Holds the context information for an audit event.
 *
 * This is a value object that captures all relevant information
 * about an auditable operation for logging purposes.
 *
 * <p>The context includes:</p>
 * <ul>
 *   <li>Entity type and ID - identifies which entity was affected</li>
 *   <li>Operation type - CREATE, UPDATE, or DELETE</li>
 *   <li>Before/After data - JSON snapshots of entity state</li>
 *   <li>Timestamp - when the operation occurred</li>
 * </ul>
 */
public record AuditContext(
    String entityType,
    Long entityId,
    OperationType operationType,
    String beforeData,
    String afterData,
    LocalDateTime timestamp
) {

    /**
     * Creates an AuditContext for a CREATE operation.
     */
    public static AuditContext forCreate(String entityType, Long entityId, String afterData) {
        return new AuditContext(
            entityType,
            entityId,
            OperationType.CREATE,
            null,
            afterData,
            LocalDateTime.now()
        );
    }

    /**
     * Creates an AuditContext for an UPDATE operation.
     */
    public static AuditContext forUpdate(String entityType, Long entityId,
                                         String beforeData, String afterData) {
        return new AuditContext(
            entityType,
            entityId,
            OperationType.UPDATE,
            beforeData,
            afterData,
            LocalDateTime.now()
        );
    }

    /**
     * Creates an AuditContext for a DELETE operation.
     */
    public static AuditContext forDelete(String entityType, Long entityId, String beforeData) {
        return new AuditContext(
            entityType,
            entityId,
            OperationType.DELETE,
            beforeData,
            null,
            LocalDateTime.now()
        );
    }

    /**
     * Builder for creating AuditContext with custom values.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String entityType;
        private Long entityId;
        private OperationType operationType;
        private String beforeData;
        private String afterData;
        private LocalDateTime timestamp = LocalDateTime.now();

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder operationType(OperationType operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder beforeData(String beforeData) {
            this.beforeData = beforeData;
            return this;
        }

        public Builder afterData(String afterData) {
            this.afterData = afterData;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditContext build() {
            return new AuditContext(entityType, entityId, operationType,
                                   beforeData, afterData, timestamp);
        }
    }
}
