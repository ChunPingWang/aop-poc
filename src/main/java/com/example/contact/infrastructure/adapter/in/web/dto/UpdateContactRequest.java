package com.example.contact.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateContactRequest(
    @NotBlank(message = "姓名為必填欄位")
    String name,

    @NotBlank(message = "電話為必填欄位")
    String phone,

    String address
) {}
