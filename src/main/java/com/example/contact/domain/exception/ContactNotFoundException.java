package com.example.contact.domain.exception;

import com.example.contact.domain.model.ContactId;

/**
 * Exception thrown when a contact is not found in the system.
 */
public class ContactNotFoundException extends RuntimeException {

    private final ContactId contactId;

    public ContactNotFoundException(ContactId contactId) {
        super("聯絡人不存在: " + contactId.value());
        this.contactId = contactId;
    }

    public ContactNotFoundException(Long id) {
        super("聯絡人不存在: " + id);
        this.contactId = null;
    }

    public ContactId getContactId() {
        return contactId;
    }
}
