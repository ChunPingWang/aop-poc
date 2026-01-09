package com.example.contact.infrastructure.adapter.in.web;

import com.example.contact.application.port.in.GetAuditLogUseCase;
import com.example.contact.domain.model.AuditLog;
import com.example.contact.domain.model.OperationType;
import com.example.contact.infrastructure.adapter.in.web.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "稽核日誌", description = "稽核日誌查詢 API")
public class AuditLogController {

    private final GetAuditLogUseCase getAuditLogUseCase;

    public AuditLogController(GetAuditLogUseCase getAuditLogUseCase) {
        this.getAuditLogUseCase = getAuditLogUseCase;
    }

    @Operation(
        summary = "查詢所有稽核日誌",
        description = "取得系統中所有稽核日誌記錄，按操作時間降序排列"
    )
    @ApiResponse(responseCode = "200", description = "成功取得稽核日誌列表")
    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLog> auditLogs = getAuditLogUseCase.getAllAuditLogs();
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "依聯絡人 ID 查詢稽核日誌",
        description = "取得指定聯絡人的所有操作記錄"
    )
    @ApiResponse(responseCode = "200", description = "成功取得該聯絡人的稽核日誌")
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByContactId(
            @Parameter(description = "聯絡人 ID", required = true, example = "1")
            @PathVariable Long contactId) {
        List<AuditLog> auditLogs = getAuditLogUseCase.getAuditLogsByContactId(contactId);
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "依操作類型查詢稽核日誌",
        description = "取得指定操作類型（CREATE、UPDATE、DELETE）的所有記錄"
    )
    @ApiResponse(responseCode = "200", description = "成功取得該類型的稽核日誌")
    @GetMapping("/operation/{operationType}")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByOperationType(
            @Parameter(description = "操作類型", required = true, example = "CREATE")
            @PathVariable OperationType operationType) {
        List<AuditLog> auditLogs = getAuditLogUseCase.getAuditLogsByOperationType(operationType);
        List<AuditLogResponse> responses = auditLogs.stream()
                .map(AuditLogResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
