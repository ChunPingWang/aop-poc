package com.example.contact.domain.event;

import java.util.Map;

/**
 * Event published when a contact is deleted.
 */
public class ContactDeletedEvent extends ContactEvent {

    public ContactDeletedEvent(Long contactId, Map<String, Object> snapshot) {
        super(contactId, snapshot);
    }
}
