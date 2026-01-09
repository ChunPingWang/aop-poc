package com.example.contact.infrastructure.audit;

import com.example.contact.domain.model.Contact;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * AuditDataExtractor implementation for Contact entities.
 *
 * <p>This extractor provides custom logic for extracting audit information
 * from Contact domain entities, including:</p>
 * <ul>
 *   <li>Extracting the contact ID from ContactId value object</li>
 *   <li>Creating snapshots that only include audit-relevant fields</li>
 * </ul>
 */
@Component
public class ContactAuditDataExtractor implements AuditDataExtractor {

    @Override
    public boolean supports(Class<?> entityClass) {
        return Contact.class.isAssignableFrom(entityClass);
    }

    @Override
    public Optional<Long> extractEntityId(Object entity) {
        if (entity instanceof Contact contact) {
            return Optional.ofNullable(contact.getId())
                           .map(id -> id.value());
        }
        return Optional.empty();
    }

    @Override
    public Object createSnapshot(Object entity) {
        if (entity instanceof Contact contact) {
            return Map.of(
                "id", contact.getId() != null ? contact.getId().value() : null,
                "name", contact.getName(),
                "phone", contact.getPhone(),
                "address", contact.getAddress() != null ? contact.getAddress() : ""
            );
        }
        return entity;
    }

    @Override
    public String getEntityType(Object entity) {
        return "Contact";
    }

    @Override
    public int getOrder() {
        return 0; // High priority for Contact entities
    }
}
