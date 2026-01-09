package com.example.contact.integration;

import com.example.contact.CucumberSpringConfiguration;
import com.example.contact.domain.model.OperationType;
import com.example.contact.infrastructure.adapter.in.web.dto.ContactResponse;
import com.example.contact.infrastructure.adapter.out.persistence.AuditLogJpaRepository;
import com.example.contact.infrastructure.adapter.out.persistence.ContactJpaRepository;
import com.example.contact.infrastructure.adapter.out.persistence.entity.AuditLogJpaEntity;
import com.example.contact.infrastructure.adapter.out.persistence.entity.ContactJpaEntity;
import io.cucumber.java.Before;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.而且;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditLogStepDefinitions extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContactJpaRepository contactRepository;

    @Autowired
    private AuditLogJpaRepository auditLogRepository;

    private ResponseEntity<ContactResponse> contactResponse;
    private Long savedContactId;
    private AuditLogJpaEntity lastAuditLog;

    @Before("@audit")
    public void setupAudit() {
        auditLogRepository.deleteAll();
        contactRepository.deleteAll();
    }

    // === 假設 (Given) ===
    // Note: 「系統中已存在聯絡人「王小明」」 is defined in ContactStepDefinitions
    // Note: 「系統中已存在聯絡人「李四」」 uses a unique name for audit tests

    @假設("系統中已存在測試聯絡人「李四」")
    public void contactLiSiExistsForAudit() {
        ContactJpaEntity entity = new ContactJpaEntity(
            null, "李四", "0912345678", "台北市",
            LocalDateTime.now(), LocalDateTime.now()
        );
        ContactJpaEntity saved = contactRepository.save(entity);
        savedContactId = saved.getId();
    }

    // === 那麼 (Then) ===

    @那麼("系統自動產生一筆 {word} 類型的稽核日誌")
    public void auditLogCreatedWithType(String operationType) {
        OperationType type = OperationType.valueOf(operationType);

        // Wait briefly for async processing if needed
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Optional<AuditLogJpaEntity> auditLog = auditLogRepository.findByOperationType(type).stream().findFirst();

        assertThat(auditLog).isPresent();
        lastAuditLog = auditLog.get();
        assertThat(lastAuditLog.getOperationType()).isEqualTo(type);
    }

    // === 而且 (And) ===

    @而且("稽核日誌包含新增的聯絡人 ID")
    public void auditLogContainsCreatedContactId() {
        assertThat(lastAuditLog).isNotNull();
        assertThat(lastAuditLog.getContactId()).isNotNull();
        assertThat(lastAuditLog.getContactId()).isGreaterThan(0L);
    }

    @而且("稽核日誌包含修改的聯絡人 ID")
    public void auditLogContainsUpdatedContactId() {
        assertThat(lastAuditLog).isNotNull();
        assertThat(lastAuditLog.getContactId()).isNotNull();
    }

    @而且("稽核日誌包含刪除的聯絡人 ID")
    public void auditLogContainsDeletedContactId() {
        assertThat(lastAuditLog).isNotNull();
        assertThat(lastAuditLog.getContactId()).isNotNull();
    }

    @而且("稽核日誌包含操作時間")
    public void auditLogContainsOperationTime() {
        assertThat(lastAuditLog).isNotNull();
        assertThat(lastAuditLog.getOperationTime()).isNotNull();
        assertThat(lastAuditLog.getOperationTime()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @而且("稽核日誌記錄變更前後的資料")
    public void auditLogContainsBeforeAfterData() {
        assertThat(lastAuditLog).isNotNull();
        assertThat(lastAuditLog.getBeforeData()).isNotNull();
        assertThat(lastAuditLog.getAfterData()).isNotNull();
        // Before data should contain original name
        assertThat(lastAuditLog.getBeforeData()).contains("王小明");
        // After data should contain new name
        assertThat(lastAuditLog.getAfterData()).contains("王大明");
    }
}
