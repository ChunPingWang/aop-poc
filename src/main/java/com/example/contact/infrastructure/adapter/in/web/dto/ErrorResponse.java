package com.example.contact.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp,
    String path,
    List<ValidationError> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, LocalDateTime.now(), path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationError> validationErrors) {
        this(status, error, message, LocalDateTime.now(), path, validationErrors);
    }
}
