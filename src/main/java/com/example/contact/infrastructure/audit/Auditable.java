package com.example.contact.infrastructure.audit;

import com.example.contact.domain.model.OperationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for automatic audit logging.
 *
 * When applied to a service method, the audit system will automatically:
 * <ul>
 *   <li>Capture the entity state before the operation (for UPDATE/DELETE)</li>
 *   <li>Execute the method</li>
 *   <li>Capture the entity state after the operation (for CREATE/UPDATE)</li>
 *   <li>Persist an audit log entry</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 *
 * <pre>
 * // Basic usage - entity type auto-detected from return type
 * &#64;Auditable(operation = OperationType.CREATE)
 * public Contact createContact(CreateContactCommand command) { ... }
 *
 * // Explicit entity type
 * &#64;Auditable(operation = OperationType.UPDATE, entityType = "Contact")
 * public Contact updateContact(UpdateContactCommand command) { ... }
 *
 * // For void methods (like delete)
 * &#64;Auditable(operation = OperationType.DELETE, entityType = "Contact")
 * public void deleteContact(ContactId id) { ... }
 * </pre>
 *
 * <h3>Requirements:</h3>
 * <ul>
 *   <li>For CREATE: Method should return an {@link AuditableEntity}</li>
 *   <li>For UPDATE: First argument should contain the entity ID</li>
 *   <li>For DELETE: First argument should be the entity ID</li>
 * </ul>
 *
 * @see AuditableEntity
 * @see AuditContext
 * @see AuditEventHandler
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /**
     * The type of operation being performed.
     * This determines how before/after states are captured.
     *
     * @return the operation type
     */
    OperationType operation();

    /**
     * The entity type name for the audit log.
     * If not specified, it will be inferred from the return type
     * or the method's context.
     *
     * @return the entity type name, or empty string for auto-detection
     */
    String entityType() default "";

    /**
     * Whether to capture the entity state before the operation.
     * Default is true for UPDATE and DELETE operations.
     *
     * @return true to capture before state
     */
    boolean captureBeforeState() default true;

    /**
     * Whether to capture the entity state after the operation.
     * Default is true for CREATE and UPDATE operations.
     *
     * @return true to capture after state
     */
    boolean captureAfterState() default true;

    /**
     * Whether to proceed with the operation if audit logging fails.
     * If false, any audit failure will cause the operation to fail.
     * Default is true (operation continues even if audit fails).
     *
     * @return true to continue on audit failure
     */
    boolean continueOnAuditFailure() default true;
}
