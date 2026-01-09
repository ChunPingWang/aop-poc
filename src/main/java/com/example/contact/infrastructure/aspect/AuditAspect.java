package com.example.contact.infrastructure.aspect;

import com.example.contact.domain.model.ContactId;
import com.example.contact.domain.model.OperationType;
import com.example.contact.infrastructure.audit.Auditable;
import com.example.contact.infrastructure.audit.AuditContext;
import com.example.contact.infrastructure.audit.AuditDataExtractor;
import com.example.contact.infrastructure.audit.AuditEventHandler;
import com.example.contact.infrastructure.audit.AuditableEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Aspect for automatically recording audit logs when operations
 * are performed on entities.
 *
 * <p>This aspect intercepts methods annotated with {@link Auditable}
 * and creates audit logs using the configured handlers and extractors.</p>
 *
 * <h3>Extension Points:</h3>
 * <ul>
 *   <li>{@link AuditDataExtractor} - Custom logic for extracting audit data</li>
 *   <li>{@link AuditEventHandler} - Custom handling of audit events</li>
 *   <li>{@link AuditableEntity} - Entities that provide their own audit data</li>
 * </ul>
 *
 * @see Auditable
 * @see AuditContext
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger LOG = LoggerFactory.getLogger(AuditAspect.class);

    private final List<AuditEventHandler> eventHandlers;
    private final List<AuditDataExtractor> dataExtractors;
    private final ObjectMapper objectMapper;

    public AuditAspect(List<AuditEventHandler> eventHandlers,
                       List<AuditDataExtractor> dataExtractors,
                       ObjectMapper objectMapper) {
        // Sort by order - lower values have higher priority
        this.eventHandlers = eventHandlers.stream()
            .sorted(Comparator.comparingInt(AuditEventHandler::getOrder))
            .toList();
        this.dataExtractors = dataExtractors.stream()
            .sorted(Comparator.comparingInt(AuditDataExtractor::getOrder))
            .toList();
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        OperationType operation = auditable.operation();
        String entityType = determineEntityType(auditable, joinPoint);
        String beforeData = null;
        Long entityId = null;

        // For UPDATE and DELETE, capture before state
        if (auditable.captureBeforeState()
            && (operation == OperationType.UPDATE || operation == OperationType.DELETE)) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                entityId = extractEntityId(args[0]);
                if (entityId != null) {
                    beforeData = captureEntityState(entityId, joinPoint);
                }
            }
        }

        // Execute the method
        Object result = joinPoint.proceed();

        try {
            // Capture after state
            String afterData = null;
            if (auditable.captureAfterState()
                && (operation == OperationType.CREATE || operation == OperationType.UPDATE)) {
                if (result != null) {
                    entityId = extractEntityIdFromResult(result);
                    afterData = toJson(result);
                    if (entityType.isEmpty()) {
                        entityType = determineEntityTypeFromResult(result);
                    }
                }
            }

            // Create and dispatch audit event
            if (entityId != null) {
                AuditContext context = createAuditContext(
                    entityType, entityId, operation, beforeData, afterData);
                dispatchToHandlers(context, entityType);
            }
        } catch (Exception e) {
            LOG.error("Audit logging failed for {} operation: {}", operation, e.getMessage());
            if (!auditable.continueOnAuditFailure()) {
                throw e;
            }
        }

        return result;
    }

    private String determineEntityType(Auditable auditable, ProceedingJoinPoint joinPoint) {
        if (!auditable.entityType().isEmpty()) {
            return auditable.entityType();
        }
        // Try to infer from method signature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        if (returnType != void.class) {
            return returnType.getSimpleName();
        }
        return "";
    }

    private String determineEntityTypeFromResult(Object result) {
        // Check if entity implements AuditableEntity
        if (result instanceof AuditableEntity auditableEntity) {
            return auditableEntity.getEntityType();
        }
        // Find matching extractor
        for (AuditDataExtractor extractor : dataExtractors) {
            if (extractor.supports(result.getClass())) {
                return extractor.getEntityType(result);
            }
        }
        return result.getClass().getSimpleName();
    }

    private Long extractEntityId(Object arg) {
        // Handle ContactId value object
        if (arg instanceof ContactId contactId) {
            return contactId.value();
        }

        // Try to extract from command objects using reflection
        try {
            Method method = arg.getClass().getMethod("id");
            Object id = method.invoke(arg);
            if (id instanceof ContactId contactId) {
                return contactId.value();
            }
            if (id instanceof Long longId) {
                return longId;
            }
        } catch (Exception e) {
            LOG.debug("Could not extract entity ID from argument: {}", e.getMessage());
        }

        return null;
    }

    private Long extractEntityIdFromResult(Object result) {
        // Check if entity implements AuditableEntity
        if (result instanceof AuditableEntity auditableEntity) {
            return auditableEntity.getEntityId();
        }

        // Try extractors
        for (AuditDataExtractor extractor : dataExtractors) {
            if (extractor.supports(result.getClass())) {
                Optional<Long> id = extractor.extractEntityId(result);
                if (id.isPresent()) {
                    return id.get();
                }
            }
        }

        return null;
    }

    private String captureEntityState(Long entityId, ProceedingJoinPoint joinPoint) {
        try {
            Object target = joinPoint.getTarget();
            // Try common method names for fetching entities
            for (String methodName : List.of("getContactById", "findById", "getById")) {
                try {
                    for (Method method : target.getClass().getMethods()) {
                        if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                            Object param = createIdParameter(method.getParameterTypes()[0], entityId);
                            if (param != null) {
                                Object entity = method.invoke(target, param);
                                return toJson(entity);
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Try next method
                }
            }
        } catch (Exception e) {
            LOG.debug("Could not capture entity state: {}", e.getMessage());
        }
        return null;
    }

    private Object createIdParameter(Class<?> paramType, Long entityId) {
        if (paramType == ContactId.class) {
            return new ContactId(entityId);
        }
        if (paramType == Long.class || paramType == long.class) {
            return entityId;
        }
        return null;
    }

    private String toJson(Object entity) {
        try {
            Object snapshot = createSnapshot(entity);
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize entity to JSON", e);
            return null;
        }
    }

    private Object createSnapshot(Object entity) {
        // Check if entity implements AuditableEntity
        if (entity instanceof AuditableEntity auditableEntity) {
            return auditableEntity.toAuditSnapshot();
        }

        // Try extractors
        for (AuditDataExtractor extractor : dataExtractors) {
            if (extractor.supports(entity.getClass())) {
                return extractor.createSnapshot(entity);
            }
        }

        // Fallback to the entity itself
        return entity;
    }

    private AuditContext createAuditContext(String entityType, Long entityId,
                                            OperationType operation,
                                            String beforeData, String afterData) {
        return switch (operation) {
            case CREATE -> AuditContext.forCreate(entityType, entityId, afterData);
            case UPDATE -> AuditContext.forUpdate(entityType, entityId, beforeData, afterData);
            case DELETE -> AuditContext.forDelete(entityType, entityId, beforeData);
            case READ -> AuditContext.builder()
                .entityType(entityType)
                .entityId(entityId)
                .operationType(operation)
                .build();
        };
    }

    private void dispatchToHandlers(AuditContext context, String entityType) {
        for (AuditEventHandler handler : eventHandlers) {
            if (handler.supports(entityType)) {
                try {
                    handler.handle(context);
                } catch (Exception e) {
                    LOG.error("Audit event handler {} failed: {}",
                        handler.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
