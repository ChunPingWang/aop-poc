package com.example.contact.domain.model;

import com.example.contact.domain.exception.ValidationException;
import com.example.contact.infrastructure.audit.AuditableEntity;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contact domain entity - Aggregate Root.
 * Pure domain object with no framework dependencies.
 * Implements AuditableEntity for automatic audit logging.
 */
public class Contact implements AuditableEntity {

    private static final int NAME_MAX_LENGTH = 50;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int ADDRESS_MAX_LENGTH = 200;

    private final ContactId id;
    private String name;
    private String phone;
    private String address;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Contact(ContactId id, String name, String phone, String address,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method for creating a new Contact.
     * ID will be assigned after persistence.
     */
    public static Contact create(String name, String phone, String address) {
        validate(name, phone, address);
        LocalDateTime now = LocalDateTime.now();
        return new Contact(
            null,
            name.trim(),
            phone.trim(),
            address != null ? address.trim() : null,
            now,
            now
        );
    }

    /**
     * Reconstruct Contact from persistence.
     */
    public static Contact reconstitute(ContactId id, String name, String phone, String address,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Contact(id, name, phone, address, createdAt, updatedAt);
    }

    /**
     * Update contact information with validation.
     */
    public void updateInfo(String name, String phone, String address) {
        validate(name, phone, address);
        this.name = name.trim();
        this.phone = phone.trim();
        this.address = address != null ? address.trim() : null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Create a new Contact instance with the assigned ID.
     */
    public Contact withId(ContactId id) {
        return new Contact(id, this.name, this.phone, this.address, this.createdAt, this.updatedAt);
    }

    private static void validate(String name, String phone, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "姓名為必填欄位");
        }
        if (name.trim().length() > NAME_MAX_LENGTH) {
            throw new ValidationException("name", "姓名長度不可超過 " + NAME_MAX_LENGTH + " 字元");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("phone", "電話為必填欄位");
        }
        if (phone.trim().length() > PHONE_MAX_LENGTH) {
            throw new ValidationException("phone", "電話長度不可超過 " + PHONE_MAX_LENGTH + " 字元");
        }
        if (address != null && address.trim().length() > ADDRESS_MAX_LENGTH) {
            throw new ValidationException("address", "地址長度不可超過 " + ADDRESS_MAX_LENGTH + " 字元");
        }
    }

    // Getters
    public ContactId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // AuditableEntity implementation
    @Override
    public String getEntityType() {
        return "Contact";
    }

    @Override
    public Long getEntityId() {
        return id != null ? id.value() : null;
    }

    @Override
    public Object toAuditSnapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", id != null ? id.value() : null);
        snapshot.put("name", name);
        snapshot.put("phone", phone);
        snapshot.put("address", address);
        return snapshot;
    }
}
