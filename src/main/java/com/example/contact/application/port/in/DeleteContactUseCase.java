package com.example.contact.application.port.in;

import com.example.contact.domain.model.ContactId;

/**
 * Input port for deleting contacts.
 */
public interface DeleteContactUseCase {
    void deleteContact(ContactId id);
}
