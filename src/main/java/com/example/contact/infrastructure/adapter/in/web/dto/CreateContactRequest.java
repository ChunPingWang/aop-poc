package com.example.contact.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "新增聯絡人請求")
public record CreateContactRequest(
    @Schema(description = "聯絡人姓名", example = "王小明", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名為必填欄位")
    @Size(max = 50, message = "姓名長度不可超過 50 字元")
    String name,

    @Schema(description = "聯絡人電話", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "電話為必填欄位")
    @Size(max = 20, message = "電話長度不可超過 20 字元")
    String phone,

    @Schema(description = "聯絡人地址（選填）", example = "台北市信義區信義路五段7號")
    @Size(max = 200, message = "地址長度不可超過 200 字元")
    String address
) {
}
