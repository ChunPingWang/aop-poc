package com.example.contact.infrastructure.audit;

/**
 * Interface for handling audit events.
 *
 * Implement this interface to customize how audit events are processed.
 * The default implementation persists audit logs to the database,
 * but you can implement custom handlers for:
 * <ul>
 *   <li>Sending audit events to a message queue</li>
 *   <li>Writing to external audit systems</li>
 *   <li>Publishing to event streams</li>
 *   <li>Async processing with Spring Events</li>
 * </ul>
 *
 * <h3>Example: Custom Event Handler</h3>
 * <pre>
 * &#64;Component
 * public class KafkaAuditEventHandler implements AuditEventHandler {
 *
 *     private final KafkaTemplate&lt;String, AuditContext&gt; kafkaTemplate;
 *
 *     &#64;Override
 *     public void handle(AuditContext context) {
 *         kafkaTemplate.send("audit-events", context);
 *     }
 * }
 * </pre>
 *
 * <h3>Example: Async Handler with Spring Events</h3>
 * <pre>
 * &#64;Component
 * public class AsyncAuditEventHandler implements AuditEventHandler {
 *
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     &#64;Override
 *     public void handle(AuditContext context) {
 *         eventPublisher.publishEvent(new AuditEvent(context));
 *     }
 * }
 *
 * &#64;Component
 * public class AuditEventListener {
 *
 *     &#64;Async
 *     &#64;EventListener
 *     public void onAuditEvent(AuditEvent event) {
 *         // Process asynchronously
 *     }
 * }
 * </pre>
 *
 * @see AuditContext
 */
public interface AuditEventHandler {

    /**
     * Handles an audit event.
     *
     * @param context the audit context containing all audit information
     */
    void handle(AuditContext context);

    /**
     * Returns the order of this handler when multiple handlers exist.
     * Lower values have higher priority.
     *
     * @return the order value (default is 0)
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Returns whether this handler supports the given entity type.
     * Override this to create entity-specific handlers.
     *
     * @param entityType the entity type name
     * @return true if this handler supports the entity type
     */
    default boolean supports(String entityType) {
        return true; // Default: supports all entity types
    }
}
