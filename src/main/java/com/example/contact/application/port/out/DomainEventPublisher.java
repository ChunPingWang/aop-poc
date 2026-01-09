package com.example.contact.application.port.out;

import com.example.contact.domain.event.DomainEvent;

/**
 * Output port for publishing domain events.
 *
 * <p>This interface abstracts the event publishing mechanism,
 * allowing the application layer to publish events without
 * knowing the underlying implementation (Spring Events, Kafka, etc.).</p>
 *
 * <p>Implementations should ensure events are published after
 * the current transaction commits successfully.</p>
 */
public interface DomainEventPublisher {

    /**
     * Publishes a domain event.
     *
     * @param event the domain event to publish
     */
    void publish(DomainEvent event);
}
