package com.example.contact.integration;

import com.example.contact.CucumberSpringConfiguration;
import com.example.contact.domain.model.OperationType;
import com.example.contact.infrastructure.adapter.in.web.dto.AuditLogResponse;
import com.example.contact.infrastructure.adapter.out.persistence.AuditLogJpaRepository;
import com.example.contact.infrastructure.adapter.out.persistence.entity.AuditLogJpaEntity;
import io.cucumber.java.Before;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditQueryStepDefinitions extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditLogJpaRepository auditLogRepository;

    private ResponseEntity<List<AuditLogResponse>> auditLogListResponse;

    @Before
    public void setup() {
        auditLogRepository.deleteAll();
    }

    // === 假設 (Given) ===

    @假設("系統中已有多筆稽核日誌記錄")
    public void multipleAuditLogsExist() {
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 1L, LocalDateTime.now().minusHours(2),
            OperationType.CREATE, null, "{\"name\":\"張三\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 1L, LocalDateTime.now().minusHours(1),
            OperationType.UPDATE, "{\"name\":\"張三\"}", "{\"name\":\"張三丰\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 2L, LocalDateTime.now(),
            OperationType.CREATE, null, "{\"name\":\"李四\"}"
        ));
    }

    @假設("系統中已有聯絡人 ID 為 {int} 的多筆操作記錄")
    public void auditLogsForContactExist(int contactId) {
        auditLogRepository.save(new AuditLogJpaEntity(
            null, (long) contactId, LocalDateTime.now().minusHours(2),
            OperationType.CREATE, null, "{\"name\":\"王五\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, (long) contactId, LocalDateTime.now().minusHours(1),
            OperationType.UPDATE, "{\"name\":\"王五\"}", "{\"name\":\"王五五\"}"
        ));
    }

    @假設("系統中已有多筆不同類型的稽核日誌記錄")
    public void differentTypesAuditLogsExist() {
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 1L, LocalDateTime.now().minusHours(3),
            OperationType.CREATE, null, "{\"name\":\"測試1\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 2L, LocalDateTime.now().minusHours(2),
            OperationType.CREATE, null, "{\"name\":\"測試2\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 1L, LocalDateTime.now().minusHours(1),
            OperationType.UPDATE, "{\"name\":\"測試1\"}", "{\"name\":\"測試1更新\"}"
        ));
        auditLogRepository.save(new AuditLogJpaEntity(
            null, 3L, LocalDateTime.now(),
            OperationType.DELETE, "{\"name\":\"測試3\"}", null
        ));
    }

    @假設("系統中無任何稽核日誌記錄")
    public void noAuditLogsExist() {
        auditLogRepository.deleteAll();
        assertThat(auditLogRepository.count()).isZero();
    }

    // === 當 (When) ===

    @當("管理員發送查詢所有稽核日誌請求")
    public void queryAllAuditLogs() {
        auditLogListResponse = restTemplate.exchange(
            getBaseUrl() + "/api/audit-logs",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditLogResponse>>() {}
        );
    }

    @當("管理員以聯絡人 ID 為 {int} 發送查詢稽核日誌請求")
    public void queryAuditLogsByContactId(int contactId) {
        auditLogListResponse = restTemplate.exchange(
            getBaseUrl() + "/api/audit-logs/contact/" + contactId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditLogResponse>>() {}
        );
    }

    @當("管理員以操作類型 {word} 發送查詢稽核日誌請求")
    public void queryAuditLogsByOperationType(String operationType) {
        auditLogListResponse = restTemplate.exchange(
            getBaseUrl() + "/api/audit-logs/operation/" + operationType,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AuditLogResponse>>() {}
        );
    }

    // === 那麼 (Then) ===

    @那麼("系統回傳所有稽核日誌列表")
    public void returnAllAuditLogs() {
        assertThat(auditLogListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(auditLogListResponse.getBody()).isNotNull();
        assertThat(auditLogListResponse.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @那麼("系統回傳該聯絡人的所有稽核日誌")
    public void returnContactAuditLogs() {
        assertThat(auditLogListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(auditLogListResponse.getBody()).isNotNull();
        assertThat(auditLogListResponse.getBody().size()).isGreaterThanOrEqualTo(2);
        // All logs should be for contact ID 1
        assertThat(auditLogListResponse.getBody())
            .allMatch(log -> log.contactId() == 1L);
    }

    @那麼("系統僅回傳 {word} 類型的稽核日誌")
    public void returnOnlySpecificTypeAuditLogs(String operationType) {
        OperationType type = OperationType.valueOf(operationType);
        assertThat(auditLogListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(auditLogListResponse.getBody()).isNotNull();
        assertThat(auditLogListResponse.getBody())
            .allMatch(log -> log.operationType() == type);
    }

    @那麼("系統回傳空的稽核日誌列表")
    public void returnEmptyAuditLogList() {
        assertThat(auditLogListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(auditLogListResponse.getBody()).isNotNull();
        assertThat(auditLogListResponse.getBody()).isEmpty();
    }

    // === 而且 (And) ===

    @而且("日誌按操作時間降序排列")
    public void logsOrderedByTimeDesc() {
        List<AuditLogResponse> logs = auditLogListResponse.getBody();
        assertThat(logs).isNotNull();
        for (int i = 0; i < logs.size() - 1; i++) {
            assertThat(logs.get(i).operationTime())
                .isAfterOrEqualTo(logs.get(i + 1).operationTime());
        }
    }
}
