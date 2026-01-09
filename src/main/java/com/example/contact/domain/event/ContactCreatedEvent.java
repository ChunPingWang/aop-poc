package com.example.contact.domain.event;

import com.example.contact.domain.model.Contact;

/**
 * Event published when a new contact is created.
 */
public class ContactCreatedEvent extends ContactEvent {

    public ContactCreatedEvent(Contact contact) {
        super(contact);
    }
}
