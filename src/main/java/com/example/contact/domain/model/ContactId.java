package com.example.contact.domain.model;

import java.util.Objects;

/**
 * Value object representing a Contact's unique identifier.
 * Provides type safety to prevent ID confusion.
 */
public record ContactId(Long value) {

    public ContactId {
        Objects.requireNonNull(value, "Contact ID cannot be null");
        if (value <= 0) {
            throw new IllegalArgumentException("Contact ID must be positive");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
