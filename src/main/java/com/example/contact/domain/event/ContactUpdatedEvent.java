package com.example.contact.domain.event;

import com.example.contact.domain.model.Contact;

import java.util.Map;

/**
 * Event published when a contact is updated.
 */
public class ContactUpdatedEvent extends ContactEvent {

    private final Map<String, Object> beforeSnapshot;

    public ContactUpdatedEvent(Contact contact, Map<String, Object> beforeSnapshot) {
        super(contact);
        this.beforeSnapshot = beforeSnapshot;
    }

    public Map<String, Object> getBeforeSnapshot() {
        return beforeSnapshot;
    }
}
