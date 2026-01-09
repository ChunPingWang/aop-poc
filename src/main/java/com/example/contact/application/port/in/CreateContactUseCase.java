package com.example.contact.application.port.in;

import com.example.contact.domain.model.Contact;

/**
 * Input port for creating a new contact.
 */
public interface CreateContactUseCase {
    Contact createContact(CreateContactCommand command);
}
