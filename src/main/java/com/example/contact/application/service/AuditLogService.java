package com.example.contact.application.service;

import com.example.contact.application.port.in.GetAuditLogUseCase;
import com.example.contact.application.port.out.AuditLogRepository;
import com.example.contact.domain.model.AuditLog;
import com.example.contact.domain.model.OperationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditLogService implements GetAuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    public List<AuditLog> getAuditLogsByContactId(Long contactId) {
        return auditLogRepository.findByContactId(contactId);
    }

    @Override
    public List<AuditLog> getAuditLogsByOperationType(OperationType operationType) {
        return auditLogRepository.findByOperationType(operationType);
    }
}
