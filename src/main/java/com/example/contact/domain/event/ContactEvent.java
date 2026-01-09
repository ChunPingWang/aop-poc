package com.example.contact.domain.event;

import com.example.contact.domain.model.Contact;

import java.util.Map;

/**
 * Base class for all Contact-related domain events.
 */
public abstract class ContactEvent extends DomainEvent {

    private final Long contactId;
    private final Map<String, Object> snapshot;

    protected ContactEvent(Contact contact) {
        super();
        this.contactId = contact.getId() != null ? contact.getId().value() : null;
        this.snapshot = createSnapshot(contact);
    }

    protected ContactEvent(Long contactId, Map<String, Object> snapshot) {
        super();
        this.contactId = contactId;
        this.snapshot = snapshot;
    }

    public Long getContactId() {
        return contactId;
    }

    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    private Map<String, Object> createSnapshot(Contact contact) {
        return Map.of(
            "id", contact.getId() != null ? contact.getId().value() : "",
            "name", contact.getName(),
            "phone", contact.getPhone(),
            "address", contact.getAddress() != null ? contact.getAddress() : ""
        );
    }

    @Override
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
