package com.example.contact.infrastructure.adapter.in.web.dto;

import com.example.contact.domain.model.Contact;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "聯絡人回應")
public record ContactResponse(
    @Schema(description = "聯絡人 ID", example = "1")
    Long id,

    @Schema(description = "聯絡人姓名", example = "王小明")
    String name,

    @Schema(description = "聯絡人電話", example = "0912345678")
    String phone,

    @Schema(description = "聯絡人地址", example = "台北市信義區信義路五段7號")
    String address,

    @Schema(description = "建立時間", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "更新時間", example = "2024-01-15T10:30:00")
    LocalDateTime updatedAt
) {
    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
            contact.getId() != null ? contact.getId().value() : null,
            contact.getName(),
            contact.getPhone(),
            contact.getAddress(),
            contact.getCreatedAt(),
            contact.getUpdatedAt()
        );
    }
}
