package com.example.contact.application.port.in;

import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import java.util.List;

/**
 * Input port for retrieving contacts.
 */
public interface GetContactUseCase {
    Contact getContactById(ContactId id);
    List<Contact> getAllContacts();
}
