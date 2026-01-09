package com.example.contact.infrastructure.adapter.in.web.dto;

public record ValidationError(
    String field,
    String message
) {
}
