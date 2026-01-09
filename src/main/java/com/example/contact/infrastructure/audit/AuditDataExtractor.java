package com.example.contact.infrastructure.audit;

import java.util.Optional;

/**
 * Interface for extracting audit data from entities.
 *
 * Implement this interface to provide custom logic for extracting
 * entity IDs and creating audit snapshots for specific entity types.
 *
 * <p>This is useful when:</p>
 * <ul>
 *   <li>Your entities don't implement {@link AuditableEntity}</li>
 *   <li>You need custom serialization logic</li>
 *   <li>Entity ID extraction requires special handling</li>
 * </ul>
 *
 * <h3>Example Implementation:</h3>
 * <pre>
 * &#64;Component
 * public class ContactAuditDataExtractor implements AuditDataExtractor {
 *
 *     &#64;Override
 *     public boolean supports(Class&lt;?&gt; entityClass) {
 *         return Contact.class.isAssignableFrom(entityClass);
 *     }
 *
 *     &#64;Override
 *     public Optional&lt;Long&gt; extractEntityId(Object entity) {
 *         if (entity instanceof Contact contact) {
 *             return Optional.ofNullable(contact.getId())
 *                            .map(ContactId::value);
 *         }
 *         return Optional.empty();
 *     }
 *
 *     &#64;Override
 *     public Object createSnapshot(Object entity) {
 *         if (entity instanceof Contact contact) {
 *             return new ContactSnapshot(
 *                 contact.getId().value(),
 *                 contact.getName(),
 *                 contact.getPhone(),
 *                 contact.getAddress()
 *             );
 *         }
 *         return entity;
 *     }
 * }
 * </pre>
 *
 * @see AuditableEntity
 */
public interface AuditDataExtractor {

    /**
     * Checks if this extractor supports the given entity class.
     *
     * @param entityClass the entity class to check
     * @return true if this extractor can handle the entity
     */
    boolean supports(Class<?> entityClass);

    /**
     * Extracts the entity ID from the given entity.
     *
     * @param entity the entity to extract ID from
     * @return the entity ID, or empty if not available
     */
    Optional<Long> extractEntityId(Object entity);

    /**
     * Creates a snapshot of the entity for audit logging.
     * The snapshot should be serializable to JSON.
     *
     * @param entity the entity to create snapshot from
     * @return a snapshot object representing the entity state
     */
    Object createSnapshot(Object entity);

    /**
     * Returns the entity type name for the given entity.
     *
     * @param entity the entity
     * @return the entity type name
     */
    default String getEntityType(Object entity) {
        return entity.getClass().getSimpleName();
    }

    /**
     * Returns the order of this extractor when multiple extractors match.
     * Lower values have higher priority.
     *
     * @return the order value (default is 0)
     */
    default int getOrder() {
        return 0;
    }
}
