package com.example.contact.application.port.in;

import com.example.contact.domain.model.ContactId;

/**
 * Command object for updating an existing contact.
 */
public record UpdateContactCommand(
    ContactId id,
    String name,
    String phone,
    String address
) {
    public UpdateContactCommand {
        if (id == null) {
            throw new IllegalArgumentException("Contact ID is required");
        }
    }
}
