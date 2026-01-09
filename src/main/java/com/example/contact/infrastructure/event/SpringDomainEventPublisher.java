package com.example.contact.infrastructure.event;

import com.example.contact.application.port.out.DomainEventPublisher;
import com.example.contact.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of DomainEventPublisher.
 *
 * <p>Uses Spring's ApplicationEventPublisher to publish domain events.
 * Events can be consumed by any Spring @EventListener or
 * @TransactionalEventListener.</p>
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        LOG.debug("Publishing domain event: {} with ID {}",
            event.getEventType(), event.getEventId());
        applicationEventPublisher.publishEvent(event);
    }
}
