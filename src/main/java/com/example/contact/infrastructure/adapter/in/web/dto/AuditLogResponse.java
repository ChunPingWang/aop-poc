package com.example.contact.infrastructure.adapter.in.web.dto;

import com.example.contact.domain.model.AuditLog;
import com.example.contact.domain.model.OperationType;

import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    Long contactId,
    LocalDateTime operationTime,
    OperationType operationType,
    String beforeData,
    String afterData
) {
    public static AuditLogResponse from(AuditLog auditLog) {
        return new AuditLogResponse(
            auditLog.getId(),
            auditLog.getContactId(),
            auditLog.getOperationTime(),
            auditLog.getOperationType(),
            auditLog.getBeforeData(),
            auditLog.getAfterData()
        );
    }
}
