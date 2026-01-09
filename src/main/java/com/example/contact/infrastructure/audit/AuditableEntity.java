package com.example.contact.infrastructure.audit;

/**
 * Interface for entities that can be audited.
 *
 * Implement this interface on domain entities to enable automatic audit logging.
 * The audit system will use the methods defined here to extract audit information.
 *
 * <p>Example usage:</p>
 * <pre>
 * public class Contact implements AuditableEntity {
 *
 *     &#64;Override
 *     public String getEntityType() {
 *         return "Contact";
 *     }
 *
 *     &#64;Override
 *     public Long getEntityId() {
 *         return this.id.value();
 *     }
 *
 *     &#64;Override
 *     public Object toAuditSnapshot() {
 *         return Map.of(
 *             "id", id.value(),
 *             "name", name,
 *             "phone", phone,
 *             "address", address
 *         );
 *     }
 * }
 * </pre>
 */
public interface AuditableEntity {

    /**
     * Returns the entity type name for audit logging.
     * This should be a simple, readable name like "Contact", "Order", etc.
     *
     * @return the entity type name
     */
    String getEntityType();

    /**
     * Returns the unique identifier of this entity.
     * This is used to track which specific entity was modified.
     *
     * @return the entity ID, or null if not yet persisted
     */
    Long getEntityId();

    /**
     * Creates a snapshot of the entity's current state for audit logging.
     * This snapshot will be serialized to JSON and stored in the audit log.
     *
     * The returned object should contain all fields that are relevant for auditing.
     * Consider using a Map, Record, or a dedicated snapshot class.
     *
     * @return an object representing the entity's current state
     */
    Object toAuditSnapshot();
}
