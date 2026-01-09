package com.example.contact.application.port.in;

import com.example.contact.domain.model.Contact;

/**
 * Input port for updating contacts.
 */
public interface UpdateContactUseCase {
    Contact updateContact(UpdateContactCommand command);
}
