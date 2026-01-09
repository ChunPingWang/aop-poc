package com.example.contact.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events.
 *
 * <p>Domain events represent something meaningful that happened in the domain.
 * They are immutable and carry all the information needed to describe
 * what happened.</p>
 */
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    /**
     * Returns the type of this event for logging/auditing purposes.
     */
    public abstract String getEventType();
}
