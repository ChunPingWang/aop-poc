# 聯絡人管理系統 - 技術規格書 (TECH)

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件版本 | 2.0 |
| 建立日期 | 2026-01-09 |
| 對應 PRD 版本 | 1.0 |
| 技術架構師 | - |

---

## 2. 技術架構概覽

### 2.1 架構依賴規則

```
┌─────────────────────────────────────────────────────────────────┐
│                      依賴方向規則                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Infrastructure Layer  ───────────►  Application Layer         │
│          (可以依賴)                        (被依賴)              │
│                                                                 │
│   Application Layer     ───────────►  Domain Layer              │
│          (可以依賴)                        (被依賴)              │
│                                                                 │
│   ✗ Application Layer   ─────X─────►  Infrastructure Layer      │
│          (禁止依賴)                                              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

規則說明：
1. Domain Layer 為核心，不依賴任何外層
2. Application Layer 只依賴 Domain Layer
3. Infrastructure Layer 可依賴 Application Layer 和 Domain Layer
4. 透過 Output Port (Interface) 實現依賴反轉
```

### 2.2 六角形架構 (Hexagonal Architecture)

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│                    (Postman / Browser / App)                     │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ HTTP/REST
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                      │
│                                                                  │
│  ┌─────────────────── Infrastructure Layer ──────────────────┐  │
│  │                                                            │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │              Input Adapters (Driving)                │  │  │
│  │  │  ┌─────────────────┐   ┌─────────────────────────┐  │  │  │
│  │  │  │ContactController│   │  AuditLogController     │  │  │  │
│  │  │  └────────┬────────┘   └────────────┬────────────┘  │  │  │
│  │  └───────────┼─────────────────────────┼───────────────┘  │  │
│  │              │                         │                   │  │
│  │  ┌───────────┼─────────────────────────┼───────────────┐  │  │
│  │  │           │   Event Infrastructure  │               │  │  │
│  │  │  ┌────────▼─────────┐  ┌────────────▼────────────┐  │  │  │
│  │  │  │SpringDomainEvent │  │   AuditEventListener    │  │  │  │
│  │  │  │   Publisher      │  │ @TransactionalEvent     │  │  │  │
│  │  │  └──────────────────┘  └─────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────────┘  │
│                              │                                    │
│  ┌───────────────── Application Layer ────────────────────────┐  │
│  │                           │                                 │  │
│  │  ┌────────────────────────┼─────────────────────────────┐  │  │
│  │  │        Input Ports     │       Output Ports          │  │  │
│  │  │  ┌─────────────────┐   │   ┌─────────────────────┐   │  │  │
│  │  │  │ CreateContact   │   │   │ ContactRepository   │   │  │  │
│  │  │  │ UseCase         │   │   │ (interface)         │   │  │  │
│  │  │  ├─────────────────┤   │   ├─────────────────────┤   │  │  │
│  │  │  │ UpdateContact   │   │   │ AuditLogRepository  │   │  │  │
│  │  │  │ UseCase         │   │   │ (interface)         │   │  │  │
│  │  │  ├─────────────────┤   │   ├─────────────────────┤   │  │  │
│  │  │  │ DeleteContact   │   │   │ DomainEventPublisher│   │  │  │
│  │  │  │ UseCase         │   │   │ (interface)         │   │  │  │
│  │  │  └─────────────────┘   │   └─────────────────────┘   │  │  │
│  │  └────────────────────────┼─────────────────────────────┘  │  │
│  │                           │                                 │  │
│  │  ┌────────────────────────┼─────────────────────────────┐  │  │
│  │  │               Service Layer                           │  │  │
│  │  │  ┌─────────────────────▼───────────────────────────┐  │  │  │
│  │  │  │              ContactService                      │  │  │  │
│  │  │  │  - implements Use Cases                          │  │  │  │
│  │  │  │  - depends on Output Ports (interfaces)          │  │  │  │
│  │  │  │  - publishes Domain Events                       │  │  │  │
│  │  │  └─────────────────────────────────────────────────┘  │  │  │
│  │  └───────────────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                    │
│  ┌───────────────────── Domain Layer ─────────────────────────┐  │
│  │                           │                                 │  │
│  │  ┌─────────────────┐  ┌───┴───────────────┐  ┌───────────┐ │  │
│  │  │    Entities     │  │   Domain Events   │  │ Exceptions│ │  │
│  │  │ ┌─────────────┐ │  │ ┌───────────────┐ │  │           │ │  │
│  │  │ │   Contact   │ │  │ │ContactCreated │ │  │ ContactNot│ │  │
│  │  │ │   AuditLog  │ │  │ │ContactUpdated │ │  │ Found     │ │  │
│  │  │ │   ContactId │ │  │ │ContactDeleted │ │  │ Validation│ │  │
│  │  │ └─────────────┘ │  │ └───────────────┘ │  │ Exception │ │  │
│  │  └─────────────────┘  └───────────────────┘  └───────────┘ │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                              │                                    │
│  ┌─────────────────── Infrastructure Layer ───────────────────┐  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │              Output Adapters (Driven)                │   │  │
│  │  │  ┌─────────────────┐   ┌─────────────────────────┐  │   │  │
│  │  │  │ContactJpaAdapter│   │  AuditLogJpaAdapter     │  │   │  │
│  │  │  │(implements port)│   │  (implements port)      │  │   │  │
│  │  │  └────────┬────────┘   └────────────┬────────────┘  │   │  │
│  │  └───────────┼─────────────────────────┼───────────────┘   │  │
│  └──────────────┼─────────────────────────┼───────────────────┘  │
└─────────────────┼─────────────────────────┼──────────────────────┘
                  ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    H2 Database (In-Memory)                       │
│  ┌─────────────────────┐      ┌─────────────────────────────┐   │
│  │   CONTACTS Table    │      │      AUDIT_LOGS Table       │   │
│  └─────────────────────┘      └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 技術堆疊

| 層級 | 技術選型 | 版本 |
|------|----------|------|
| 語言 | Java | 17+ |
| 框架 | Spring Boot | 3.2.x |
| Web | Spring Web MVC | - |
| ORM | Spring Data JPA | - |
| Event | Spring ApplicationEvent | - |
| 資料庫 | H2 Database | - |
| 建構工具 | Gradle | 8.x |
| 測試 | Cucumber BDD, JUnit 5 | - |
| API 文件 | Springdoc OpenAPI (optional) | 2.x |

---

## 3. 專案結構

採用六角形架構 (Hexagonal Architecture) 組織程式碼：

```
contact-management/
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/contact/
│   │   │       ├── ContactApplication.java
│   │   │       │
│   │   │       ├── domain/                    # Domain Layer (核心)
│   │   │       │   ├── model/
│   │   │       │   │   ├── Contact.java       # Aggregate Root
│   │   │       │   │   ├── ContactId.java     # Value Object
│   │   │       │   │   ├── AuditLog.java
│   │   │       │   │   └── OperationType.java
│   │   │       │   ├── event/                 # Domain Events
│   │   │       │   │   ├── DomainEvent.java
│   │   │       │   │   ├── ContactEvent.java
│   │   │       │   │   ├── ContactCreatedEvent.java
│   │   │       │   │   ├── ContactUpdatedEvent.java
│   │   │       │   │   └── ContactDeletedEvent.java
│   │   │       │   └── exception/
│   │   │       │       ├── ContactNotFoundException.java
│   │   │       │       └── ValidationException.java
│   │   │       │
│   │   │       ├── application/               # Application Layer
│   │   │       │   ├── port/
│   │   │       │   │   ├── in/                # Input Ports (Use Cases)
│   │   │       │   │   │   ├── CreateContactUseCase.java
│   │   │       │   │   │   ├── GetContactUseCase.java
│   │   │       │   │   │   ├── UpdateContactUseCase.java
│   │   │       │   │   │   └── DeleteContactUseCase.java
│   │   │       │   │   └── out/               # Output Ports (Interfaces)
│   │   │       │   │       ├── ContactRepository.java
│   │   │       │   │       ├── AuditLogRepository.java
│   │   │       │   │       └── DomainEventPublisher.java
│   │   │       │   └── service/
│   │   │       │       ├── ContactService.java
│   │   │       │       └── AuditLogService.java
│   │   │       │
│   │   │       └── infrastructure/            # Infrastructure Layer
│   │   │           ├── adapter/
│   │   │           │   ├── in/                # Input Adapters
│   │   │           │   │   └── web/
│   │   │           │   │       ├── ContactController.java
│   │   │           │   │       └── AuditLogController.java
│   │   │           │   └── out/               # Output Adapters
│   │   │           │       └── persistence/
│   │   │           │           ├── ContactJpaAdapter.java
│   │   │           │           ├── AuditLogJpaAdapter.java
│   │   │           │           ├── ContactJpaEntity.java
│   │   │           │           └── AuditLogJpaEntity.java
│   │   │           ├── event/                 # Event Infrastructure
│   │   │           │   ├── SpringDomainEventPublisher.java
│   │   │           │   └── AuditEventListener.java
│   │   │           └── config/
│   │   │               └── JpaConfig.java
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│       └── java/
│           └── com/example/contact/
│               ├── unit/                      # 單元測試
│               │   ├── domain/
│               │   │   └── ContactTest.java
│               │   └── application/
│               │       └── ContactServiceTest.java
│               ├── integration/               # 整合測試
│               │   └── ContactControllerTest.java
│               └── acceptance/                # BDD 驗收測試
│                   ├── CucumberTestRunner.java
│                   └── steps/
│                       └── ContactSteps.java
```

---

## 4. 資料庫設計

### 4.1 ER Diagram

```
┌──────────────────────────┐          ┌─────────────────────────────────┐
│        CONTACTS          │          │          AUDIT_LOGS             │
├──────────────────────────┤          ├─────────────────────────────────┤
│ PK  id          BIGINT   │◄────────┐│ PK  id              BIGINT      │
│     name        VARCHAR  │         ││     contact_id      BIGINT      │──┐
│     phone       VARCHAR  │         ││     operation_type  VARCHAR     │  │
│     address     VARCHAR  │         ││     before_data     CLOB        │  │
│     created_at  TIMESTAMP│         ││     after_data      CLOB        │  │
│     updated_at  TIMESTAMP│         ││     created_at      TIMESTAMP   │  │
└──────────────────────────┘         │└─────────────────────────────────┘  │
                                     │                                     │
                                     └─────────────────────────────────────┘
                                           (Logical Reference)
```

### 4.2 DDL Scripts

```sql
-- Contact Table
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log Table (Domain Event Based)
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    operation_type VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE
    before_data CLOB,                     -- JSON: 操作前的資料快照
    after_data CLOB,                      -- JSON: 操作後的資料快照
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Audit Log queries
CREATE INDEX idx_audit_logs_contact_id ON audit_logs(contact_id);
CREATE INDEX idx_audit_logs_operation_type ON audit_logs(operation_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

### 4.3 稽核資料範例

```json
// CREATE 操作 - before_data 為 null
{
  "operation_type": "CREATE",
  "before_data": null,
  "after_data": {"name": "張三", "phone": "0912345678", "address": "台北市"}
}

// UPDATE 操作 - 記錄變更前後狀態
{
  "operation_type": "UPDATE",
  "before_data": {"name": "張三", "phone": "0912345678", "address": "台北市"},
  "after_data": {"name": "張三", "phone": "0987654321", "address": "新北市"}
}

// DELETE 操作 - after_data 為 null
{
  "operation_type": "DELETE",
  "before_data": {"name": "張三", "phone": "0987654321", "address": "新北市"},
  "after_data": null
}
```

---

## 5. 核心元件設計

### 5.1 Domain Layer

#### Contact.java (Domain Entity - Aggregate Root)

```java
/**
 * Contact domain entity - Aggregate Root.
 * Pure domain object with no framework dependencies.
 */
public class Contact {

    private static final int NAME_MAX_LENGTH = 50;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int ADDRESS_MAX_LENGTH = 200;

    private final ContactId id;
    private String name;
    private String phone;
    private String address;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method for creating a new Contact.
     */
    public static Contact create(String name, String phone, String address) {
        validate(name, phone, address);
        LocalDateTime now = LocalDateTime.now();
        return new Contact(null, name.trim(), phone.trim(),
            address != null ? address.trim() : null, now, now);
    }

    /**
     * Reconstruct Contact from persistence.
     */
    public static Contact reconstitute(ContactId id, String name, String phone,
            String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    private static void validate(String name, String phone, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "姓名為必填欄位");
        }
        // ... additional validations
    }

    // Getters, withId method for assigning ID after persistence
}
```

#### Domain Events

```java
/**
 * Base class for all domain events.
 */
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }

    public abstract String getEventType();
}

/**
 * Event published when a contact is created.
 */
public class ContactCreatedEvent extends ContactEvent {
    public ContactCreatedEvent(Contact contact) {
        super(contact);
    }

    @Override
    public String getEventType() {
        return "CONTACT_CREATED";
    }
}

/**
 * Event published when a contact is updated.
 * Includes before-snapshot for audit trail.
 */
public class ContactUpdatedEvent extends ContactEvent {
    private final Map<String, Object> beforeSnapshot;

    public ContactUpdatedEvent(Contact contact, Map<String, Object> beforeSnapshot) {
        super(contact);
        this.beforeSnapshot = beforeSnapshot;
    }
}
```

### 5.2 Application Layer

#### Output Ports (Interfaces)

```java
/**
 * Output port for publishing domain events.
 * Infrastructure layer provides the implementation.
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}

/**
 * Output port for contact persistence.
 */
public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(ContactId id);
    List<Contact> findAll();
    void delete(Contact contact);
}
```

#### ContactService.java

```java
/**
 * Application service that orchestrates contact operations.
 * Publishes domain events for cross-cutting concerns like auditing.
 */
@Service
@Transactional
public class ContactService implements CreateContactUseCase, GetContactUseCase,
        UpdateContactUseCase, DeleteContactUseCase {

    private final ContactRepository contactRepository;
    private final DomainEventPublisher eventPublisher;

    public ContactService(ContactRepository contactRepository,
                          DomainEventPublisher eventPublisher) {
        this.contactRepository = contactRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Contact createContact(CreateContactCommand command) {
        Contact contact = Contact.create(
            command.name(),
            command.phone(),
            command.address()
        );
        Contact savedContact = contactRepository.save(contact);

        // Publish domain event - listeners handle audit logging
        eventPublisher.publish(new ContactCreatedEvent(savedContact));

        return savedContact;
    }

    @Override
    public Contact updateContact(UpdateContactCommand command) {
        Contact contact = contactRepository.findById(new ContactId(command.id()))
            .orElseThrow(() -> new ContactNotFoundException(command.id()));

        // Capture before state for audit
        Map<String, Object> beforeSnapshot = contact.toSnapshot();

        contact.updateInfo(command.name(), command.phone(), command.address());
        Contact savedContact = contactRepository.save(contact);

        eventPublisher.publish(new ContactUpdatedEvent(savedContact, beforeSnapshot));

        return savedContact;
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(new ContactId(id))
            .orElseThrow(() -> new ContactNotFoundException(id));

        contactRepository.delete(contact);

        eventPublisher.publish(new ContactDeletedEvent(contact));
    }
}
```

### 5.3 Infrastructure Layer

#### SpringDomainEventPublisher.java

```java
/**
 * Spring implementation of DomainEventPublisher.
 * Uses Spring's ApplicationEventPublisher for event dispatching.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

---

## 6. Domain Events 稽核設計

### 6.1 設計概念

使用 Domain Events 模式將稽核日誌完全從業務邏輯解耦。業務層只發布「發生了什麼事」的事件，基礎設施層的監聽器負責處理稽核記錄。

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Domain Events Audit Flow                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────┐    ┌──────────────────┐    ┌────────────────────────┐│
│  │Controller│───►│  ContactService  │───►│  ContactRepository     ││
│  └──────────┘    │                  │    │  (save/update/delete)  ││
│                  │  ┌────────────┐  │    └────────────────────────┘│
│                  │  │ publish()  │  │                               │
│                  │  └─────┬──────┘  │                               │
│                  └────────┼─────────┘                               │
│                           │                                         │
│                           ▼                                         │
│                  ┌─────────────────────┐                            │
│                  │DomainEventPublisher │  (Output Port)             │
│                  │     (interface)     │                            │
│                  └─────────┬───────────┘                            │
│                            │                                        │
│  ─────────────────────────┬┴───────────────────────────────────────│
│  Infrastructure Layer     │                                         │
│                            ▼                                        │
│                  ┌─────────────────────────┐                        │
│                  │SpringDomainEventPublisher│                       │
│                  │  (implements port)       │                       │
│                  └─────────┬───────────────┘                        │
│                            │                                        │
│                            ▼                                        │
│                  ┌─────────────────────────┐                        │
│                  │   AuditEventListener    │                        │
│                  │ @TransactionalEventListener                      │
│                  │ (phase = BEFORE_COMMIT) │                        │
│                  └─────────┬───────────────┘                        │
│                            │                                        │
│                            ▼                                        │
│                  ┌─────────────────────────┐                        │
│                  │   AuditLogRepository    │                        │
│                  │      .save()            │                        │
│                  └─────────────────────────┘                        │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 架構優勢

| 面向 | Domain Events | AOP |
|------|--------------|-----|
| 依賴方向 | Application → Domain (正確) | Application → Infrastructure (違反) |
| 業務邏輯 | 完全純淨，只發布事件 | 需要 @Auditable 註解 |
| 可測試性 | 輕鬆 Mock DomainEventPublisher | 需處理 AOP 代理 |
| 資料完整性 | 可記錄 before/after 快照 | 僅記錄 HTTP 層資訊 |
| 事務一致性 | TransactionalEventListener 保證 | 需額外處理 |

### 6.3 Event Listener 實作

#### AuditEventListener.java

```java
/**
 * Event listener that creates audit logs in response to domain events.
 *
 * Uses TransactionalEventListener to ensure audit logs are only
 * created after the main transaction commits successfully.
 */
@Component
public class AuditEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditEventListener(AuditLogRepository auditLogRepository,
                              ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactCreated(ContactCreatedEvent event) {
        LOG.debug("Handling ContactCreatedEvent for contact ID: {}", event.getContactId());

        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.CREATE,
            null,                          // before_data is null for CREATE
            toJson(event.getSnapshot())    // after_data contains new state
        );
        auditLogRepository.save(auditLog);

        LOG.info("Audit log created for CREATE operation on contact {}", event.getContactId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactUpdated(ContactUpdatedEvent event) {
        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.UPDATE,
            toJson(event.getBeforeSnapshot()),  // before_data
            toJson(event.getSnapshot())         // after_data
        );
        auditLogRepository.save(auditLog);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onContactDeleted(ContactDeletedEvent event) {
        AuditLog auditLog = AuditLog.create(
            event.getContactId(),
            OperationType.DELETE,
            toJson(event.getSnapshot()),  // before_data (deleted state)
            null                          // after_data is null for DELETE
        );
        auditLogRepository.save(auditLog);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
}
```

### 6.4 事務一致性

使用 `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` 確保：

1. **同一事務**: 稽核日誌與業務操作在同一事務中
2. **一致性**: 業務操作失敗時，稽核日誌也會回滾
3. **順序保證**: 事件按發布順序處理

---

## 7. API 規格

### 7.1 統一回應格式

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}
```

### 7.2 API 詳細規格

#### 7.2.1 新增聯絡人

```yaml
POST /api/contacts
Content-Type: application/json

Request Body:
{
  "name": "王小明",      # 必填, 1-50 字元
  "phone": "0912345678", # 必填, 1-20 字元
  "address": "台北市..."  # 選填, 0-200 字元
}

Response (201 Created):
{
  "success": true,
  "message": "Contact created successfully",
  "data": {
    "id": 1,
    "name": "王小明",
    "phone": "0912345678",
    "address": "台北市...",
    "createdAt": "2026-01-09T10:30:00",
    "updatedAt": "2026-01-09T10:30:00"
  },
  "timestamp": "2026-01-09T10:30:00"
}
```

#### 7.2.2 查詢所有聯絡人

```yaml
GET /api/contacts

Response (200 OK):
{
  "success": true,
  "message": "Contacts retrieved",
  "data": [
    {
      "id": 1,
      "name": "王小明",
      "phone": "0912345678",
      "address": "台北市...",
      "createdAt": "2026-01-09T10:30:00",
      "updatedAt": "2026-01-09T10:30:00"
    }
  ],
  "timestamp": "2026-01-09T10:35:00"
}
```

#### 7.2.3 查詢單一聯絡人

```yaml
GET /api/contacts/{id}

Response (200 OK):
{
  "success": true,
  "message": "Contact found",
  "data": {
    "id": 1,
    "name": "王小明",
    "phone": "0912345678",
    "address": "台北市...",
    "createdAt": "2026-01-09T10:30:00",
    "updatedAt": "2026-01-09T10:30:00"
  },
  "timestamp": "2026-01-09T10:35:00"
}

Response (404 Not Found):
{
  "success": false,
  "message": "Contact not found with id: 999",
  "data": null,
  "timestamp": "2026-01-09T10:35:00"
}
```

#### 7.2.4 修改聯絡人

```yaml
PUT /api/contacts/{id}
Content-Type: application/json

Request Body:
{
  "name": "王大明",       # 選填
  "phone": "0987654321",  # 選填
  "address": "新北市..."   # 選填
}

Response (200 OK):
{
  "success": true,
  "message": "Contact updated",
  "data": {
    "id": 1,
    "name": "王大明",
    "phone": "0987654321",
    "address": "新北市...",
    "createdAt": "2026-01-09T10:30:00",
    "updatedAt": "2026-01-09T11:00:00"
  },
  "timestamp": "2026-01-09T11:00:00"
}
```

#### 7.2.5 刪除聯絡人

```yaml
DELETE /api/contacts/{id}

Response (200 OK):
{
  "success": true,
  "message": "Contact deleted",
  "data": null,
  "timestamp": "2026-01-09T11:05:00"
}
```

#### 7.2.6 查詢稽核日誌

```yaml
GET /api/audit-logs
GET /api/audit-logs?action=CREATE
GET /api/audit-logs?endpoint=/api/contacts
GET /api/audit-logs?clientIp=192.168.1.1
GET /api/audit-logs?startTime=2026-01-09T00:00:00&endTime=2026-01-09T23:59:59
GET /api/audit-logs?action=UPDATE&startTime=2026-01-09T00:00:00

Response (200 OK):
{
  "success": true,
  "message": "Audit logs retrieved",
  "data": [
    {
      "id": 1,
      "timestamp": "2026-01-09T10:30:00",
      "action": "CREATE",
      "endpoint": "/api/contacts",
      "httpMethod": "POST",
      "requestBody": "{\"name\":\"王小明\",\"phone\":\"0912345678\"}",
      "responseStatus": 201,
      "executionTime": 45,
      "clientIp": "192.168.1.100",
      "userAgent": "Mozilla/5.0..."
    }
  ],
  "timestamp": "2026-01-09T12:00:00"
}
```

---

## 8. H2 資料庫配置

### 8.1 application.yml

```yaml
spring:
  application:
    name: contact-management
    
  datasource:
    url: jdbc:h2:mem:contactdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
    
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: true  # 允許外部存取（開發環境）
        trace: false
        
  jpa:
    hibernate:
      ddl-auto: create-drop  # 或 update (保留資料)
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    database-platform: org.hibernate.dialect.H2Dialect
    
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss
    
server:
  port: 8080

logging:
  level:
    com.example.contactmanagement: DEBUG
    org.springframework.aop: DEBUG
```

### 8.2 H2 Console 存取

| 項目 | 值 |
|------|---|
| URL | http://localhost:8080/h2-console |
| JDBC URL | jdbc:h2:mem:contactdb |
| User Name | sa |
| Password | (空白) |

---

## 9. 稽核日誌查詢方式建議

### 9.1 查詢方式總覽

| 查詢方式 | API 參數 | 使用場景 | 範例 |
|----------|----------|----------|------|
| 全部查詢 | (無) | 快速瀏覽所有日誌 | `GET /api/audit-logs` |
| 時間範圍 | startTime, endTime | 定期稽核報告 | `?startTime=2026-01-01T00:00:00&endTime=2026-01-31T23:59:59` |
| 操作類型 | action | 追蹤特定操作 | `?action=DELETE` |
| API 端點 | endpoint | 分析特定 API 使用 | `?endpoint=/api/contacts` |
| 來源 IP | clientIp | 安全稽核 | `?clientIp=192.168.1.100` |
| 組合查詢 | 多個參數 | 精準定位問題 | `?action=UPDATE&startTime=2026-01-09T00:00:00` |

### 9.2 常見查詢情境

```bash
# 情境 1: 查詢今天所有的刪除操作
GET /api/audit-logs?action=DELETE&startTime=2026-01-09T00:00:00&endTime=2026-01-09T23:59:59

# 情境 2: 查詢某 IP 的所有操作（安全稽核）
GET /api/audit-logs?clientIp=192.168.1.100

# 情境 3: 查詢 Contact API 的使用情況
GET /api/audit-logs?endpoint=/api/contacts

# 情境 4: 查詢過去一小時的所有操作
GET /api/audit-logs?startTime=2026-01-09T11:00:00&endTime=2026-01-09T12:00:00

# 情境 5: 查詢特定時間內的新增操作
GET /api/audit-logs?action=CREATE&startTime=2026-01-09T00:00:00
```

---

## 10. 例外處理

### 10.1 GlobalExceptionHandler.java

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleContactNotFound(ContactNotFoundException e) {
        log.warn("Contact not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed").withData(errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error"));
    }
}
```

### 10.2 ContactNotFoundException.java

```java
public class ContactNotFoundException extends RuntimeException {
    
    public ContactNotFoundException(Long id) {
        super("Contact not found with id: " + id);
    }
}
```

---

## 11. 相依套件 (Maven)

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>contact-management</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- H2 Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok (Optional) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 12. 測試計畫

### 12.1 測試架構

採用多層次測試策略，確保各層級的正確性：

```
┌─────────────────────────────────────────────────────────┐
│                    驗收測試 (BDD)                        │
│              Cucumber + Spring Boot Test                 │
│                 測試完整業務場景                          │
├─────────────────────────────────────────────────────────┤
│                    整合測試                              │
│           @SpringBootTest + MockMvc                      │
│              測試 API 層與資料庫整合                      │
├─────────────────────────────────────────────────────────┤
│                    單元測試                              │
│              JUnit 5 + Mockito                          │
│         測試 Domain/Application 層邏輯                   │
└─────────────────────────────────────────────────────────┘
```

### 12.2 單元測試範圍

| 測試對象 | 測試內容 |
|----------|----------|
| Contact (Domain) | 實體建立、驗證規則、狀態更新 |
| ContactService | 業務邏輯、事件發布驗證 |
| AuditEventListener | 事件處理、稽核日誌建立 |

### 12.3 整合測試範圍

| 測試對象 | 測試內容 |
|----------|----------|
| ContactController | API 端點、HTTP 狀態碼、回應格式 |
| AuditLogController | 日誌查詢 API |
| Domain Events Flow | 事件發布到稽核記錄的完整流程 |

### 12.4 測試案例範例

#### 單元測試 - ContactService

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, eventPublisher);
    }

    @Test
    @DisplayName("should create and save contact successfully")
    void shouldCreateAndSaveContact() {
        // Given
        CreateContactCommand command = new CreateContactCommand("張三", "0912345678", "台北市");
        Contact savedContact = Contact.create("張三", "0912345678", "台北市")
                .withId(new ContactId(1L));

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        // When
        Contact result = contactService.createContact(command);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("張三");
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    @DisplayName("should publish ContactCreatedEvent after creation")
    void shouldPublishContactCreatedEvent() {
        // Given
        CreateContactCommand command = new CreateContactCommand("王五", "0911222333", "新北市");
        Contact savedContact = Contact.create("王五", "0911222333", "新北市")
                .withId(new ContactId(3L));

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        // When
        contactService.createContact(command);

        // Then
        ArgumentCaptor<ContactCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(ContactCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        ContactCreatedEvent event = eventCaptor.getValue();
        assertThat(event.getContactId()).isEqualTo(3L);
    }
}
```

#### BDD 驗收測試

```gherkin
# contact.feature
Feature: 聯絡人管理
  作為系統使用者
  我希望能夠管理聯絡人資料
  以便維護客戶關係

  Scenario: 成功新增聯絡人
    Given 系統已啟動
    When 我新增一位姓名為 "張三"、電話為 "0912345678" 的聯絡人
    Then 應該回傳 HTTP 狀態碼 201
    And 回傳資料中應包含聯絡人姓名 "張三"
    And 系統應該產生一筆 CREATE 類型的稽核日誌
```

---

## 13. 部署說明

### 13.1 本地執行

```bash
# 使用 Maven
mvn spring-boot:run

# 或打包後執行
mvn clean package
java -jar target/contact-management-1.0.0.jar
```

### 13.2 存取端點

| 服務 | URL |
|------|-----|
| API Base URL | http://localhost:8080/api |
| H2 Console | http://localhost:8080/h2-console |
| Health Check | http://localhost:8080/actuator/health (需加入 actuator) |

---

## 附錄 A: 快速測試指令 (cURL)

```bash
# 新增聯絡人
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -d '{"name":"王小明","phone":"0912345678","address":"台北市信義區"}'

# 查詢所有聯絡人
curl http://localhost:8080/api/contacts

# 查詢單一聯絡人
curl http://localhost:8080/api/contacts/1

# 修改聯絡人
curl -X PUT http://localhost:8080/api/contacts/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"王大明","phone":"0987654321"}'

# 刪除聯絡人
curl -X DELETE http://localhost:8080/api/contacts/1

# 查詢稽核日誌
curl http://localhost:8080/api/audit-logs

# 查詢特定操作類型的日誌
curl "http://localhost:8080/api/audit-logs?action=CREATE"

# 查詢時間範圍的日誌
curl "http://localhost:8080/api/audit-logs?startTime=2026-01-09T00:00:00&endTime=2026-01-09T23:59:59"
```

---

## 附錄 B: 架構決策記錄 (ADR)

### ADR-001: 使用 Domain Events 取代 AOP 實作稽核日誌

**狀態**: 已採用 (v2.0)

**背景**:
- 原本使用 Spring AOP 搭配 `@Auditable` 註解實作稽核日誌
- 發現此方式導致 Application Layer 依賴 Infrastructure Layer（`@Auditable` 定義在 Infrastructure）
- 違反六角形架構的依賴規則：外層應依賴內層，而非相反

**決策**: 使用 Domain Events 模式重構稽核日誌機制。

**替代方案評估**:

| 方案 | 優點 | 缺點 |
|------|------|------|
| AOP + @Auditable | 實作簡單 | 違反依賴規則、只能記錄 HTTP 層資訊 |
| JPA EntityListeners | 自動觸發 | 無法記錄 before 狀態、擴展性差 |
| Hibernate Envers | 功能完整 | 耦合 Hibernate、學習成本高 |
| **Domain Events** | **依賴正確、資料完整** | **需要手動發布事件** |

**實作細節**:
1. Domain Layer 定義事件類別 (ContactCreatedEvent, ContactUpdatedEvent, ContactDeletedEvent)
2. Application Layer 定義 Output Port (DomainEventPublisher interface)
3. Infrastructure Layer 實作事件發布器和監聽器
4. 使用 @TransactionalEventListener 確保事務一致性

**理由**:
- 依賴方向正確：Application Layer 只依賴 Domain Layer
- 資料完整性：可記錄 before/after 快照
- 關注點分離：業務邏輯不知道稽核日誌的存在
- 可測試性：輕鬆 Mock DomainEventPublisher
- 可擴展性：新增監聽器即可擴展功能（如通知、同步等）

**後果**:
- 需要在 Service 中手動發布事件
- 事件類別需要攜帶足夠的資訊供監聽器使用

---

### ADR-002: 架構依賴規則

**狀態**: 已採用

**背景**: 確保專案遵循六角形架構的依賴規則，維持架構整潔。

**決策**: 強制執行以下依賴規則：

```
┌───────────────────────────────────────────────────────┐
│                    依賴規則                            │
├───────────────────────────────────────────────────────┤
│  ✓ Infrastructure → Application  (允許)               │
│  ✓ Infrastructure → Domain       (允許)               │
│  ✓ Application → Domain          (允許)               │
│  ✗ Application → Infrastructure  (禁止)               │
│  ✗ Domain → Application          (禁止)               │
│  ✗ Domain → Infrastructure       (禁止)               │
└───────────────────────────────────────────────────────┘
```

**實作方式**:
1. 使用 Output Port (Interface) 實現依賴反轉
2. Application Layer 定義 interface，Infrastructure Layer 實作
3. 使用 Spring 依賴注入綁定實作

**理由**:
- 核心業務邏輯不受技術細節影響
- 可輕鬆替換基礎設施（如更換資料庫、訊息佇列）
- 提高可測試性

---

### ADR-003: 稽核日誌同步寫入 (Transaction 內)

**狀態**: 已採用

**背景**: 需要確保稽核日誌與業務操作的一致性。

**決策**: 使用 `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` 在交易提交前同步寫入稽核日誌。

**理由**:
- 一致性：業務操作失敗時，稽核日誌自動回滾
- 完整性：不會遺漏任何操作記錄
- 順序性：確保事件按發布順序處理

**後果**:
- 稽核日誌寫入會增加交易時間
- 如果效能成為瓶頸，可考慮改用 AFTER_COMMIT + 補償機制
