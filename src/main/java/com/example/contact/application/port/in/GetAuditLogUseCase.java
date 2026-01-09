package com.example.contact.application.port.in;

import com.example.contact.domain.model.AuditLog;
import com.example.contact.domain.model.OperationType;

import java.util.List;

/**
 * Input port for retrieving audit logs.
 */
public interface GetAuditLogUseCase {
    List<AuditLog> getAllAuditLogs();
    List<AuditLog> getAuditLogsByContactId(Long contactId);
    List<AuditLog> getAuditLogsByOperationType(OperationType operationType);
}
