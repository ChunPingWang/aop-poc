# 聯絡人管理系統 - 技術規格書 (TECH)

## 1. 文件資訊

| 項目 | 內容 |
|------|------|
| 文件版本 | 1.0 |
| 建立日期 | 2026-01-09 |
| 對應 PRD 版本 | 1.0 |
| 技術架構師 | - |

---

## 2. 技術架構概覽

### 2.1 系統架構圖

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│                    (Postman / Browser / App)                     │
└─────────────────────────────────┬───────────────────────────────┘
                                  │ HTTP/REST
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                      │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    Controller Layer                        │  │
│  │  ┌─────────────────┐        ┌─────────────────────────┐   │  │
│  │  │ ContactController│        │  AuditLogController     │   │  │
│  │  └────────┬────────┘        └────────────┬────────────┘   │  │
│  └───────────┼──────────────────────────────┼────────────────┘  │
│              │                              │                    │
│  ┌───────────┼──────────────────────────────┼────────────────┐  │
│  │           │         AOP Layer            │                │  │
│  │           │    ┌─────────────────┐       │                │  │
│  │           └───►│ AuditLogAspect  │◄──────┘                │  │
│  │                └────────┬────────┘                        │  │
│  └─────────────────────────┼─────────────────────────────────┘  │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────────┐  │
│  │                   Service Layer                            │  │
│  │  ┌─────────────────┐    │    ┌─────────────────────────┐  │  │
│  │  │ ContactService  │    │    │   AuditLogService       │  │  │
│  │  └────────┬────────┘    │    └────────────┬────────────┘  │  │
│  └───────────┼─────────────┼─────────────────┼───────────────┘  │
│              │             │                 │                   │
│  ┌───────────┼─────────────┼─────────────────┼───────────────┐  │
│  │           │      Repository Layer         │               │  │
│  │  ┌────────▼────────┐    │    ┌────────────▼────────────┐  │  │
│  │  │ContactRepository│    │    │  AuditLogRepository     │  │  │
│  │  └────────┬────────┘    │    └────────────┬────────────┘  │  │
│  └───────────┼─────────────┼─────────────────┼───────────────┘  │
└──────────────┼─────────────┼─────────────────┼───────────────────┘
               │             │                 │
               ▼             ▼                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    H2 Database (In-Memory)                       │
│  ┌─────────────────────┐      ┌─────────────────────────────┐   │
│  │   CONTACT Table     │      │      AUDIT_LOG Table        │   │
│  └─────────────────────┘      └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    H2 Console (Web UI)                           │
│                    http://localhost:8080/h2-console              │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 技術堆疊

| 層級 | 技術選型 | 版本 |
|------|----------|------|
| 語言 | Java | 17+ |
| 框架 | Spring Boot | 3.2.x |
| Web | Spring Web MVC | - |
| ORM | Spring Data JPA | - |
| AOP | Spring AOP | - |
| 資料庫 | H2 Database | - |
| 建構工具 | Maven / Gradle | - |
| API 文件 | Springdoc OpenAPI (optional) | 2.x |

---

## 3. 專案結構

```
contact-management/
├── pom.xml (or build.gradle)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/contactmanagement/
│   │   │       ├── ContactManagementApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   └── AopConfig.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── ContactController.java
│   │   │       │   └── AuditLogController.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── ContactService.java
│   │   │       │   └── AuditLogService.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ContactRepository.java
│   │   │       │   └── AuditLogRepository.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── Contact.java
│   │   │       │   └── AuditLog.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── ContactRequest.java
│   │   │       │   ├── ContactResponse.java
│   │   │       │   └── ApiResponse.java
│   │   │       │
│   │   │       ├── aspect/
│   │   │       │   └── AuditLogAspect.java
│   │   │       │
│   │   │       ├── annotation/
│   │   │       │   └── Auditable.java
│   │   │       │
│   │   │       └── exception/
│   │   │           ├── GlobalExceptionHandler.java
│   │   │           └── ContactNotFoundException.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data.sql (optional: 初始資料)
│   │
│   └── test/
│       └── java/
│           └── com/example/contactmanagement/
│               ├── controller/
│               │   └── ContactControllerTest.java
│               └── service/
│                   └── ContactServiceTest.java
```

---

## 4. 資料庫設計

### 4.1 ER Diagram

```
┌──────────────────────────┐          ┌─────────────────────────────────┐
│        CONTACT           │          │          AUDIT_LOG              │
├──────────────────────────┤          ├─────────────────────────────────┤
│ PK  id          BIGINT   │          │ PK  id              BIGINT      │
│     name        VARCHAR  │          │     timestamp       TIMESTAMP   │
│     phone       VARCHAR  │          │     action          VARCHAR     │
│     address     VARCHAR  │          │     endpoint        VARCHAR     │
│     created_at  TIMESTAMP│          │     http_method     VARCHAR     │
│     updated_at  TIMESTAMP│          │     request_body    CLOB        │
└──────────────────────────┘          │     response_status INT         │
                                      │     execution_time  BIGINT      │
                                      │     client_ip       VARCHAR     │
                                      │     user_agent      VARCHAR     │
                                      └─────────────────────────────────┘
```

### 4.2 DDL Scripts

```sql
-- Contact Table
CREATE TABLE contact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Audit Log Table
CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(20) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    request_body CLOB,
    response_status INT,
    execution_time BIGINT,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Indexes for Audit Log queries
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_endpoint ON audit_log(endpoint);
CREATE INDEX idx_audit_log_client_ip ON audit_log(client_ip);
```

---

## 5. 核心元件設計

### 5.1 Entity 類別

#### Contact.java

```java
@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener.class)
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(nullable = false, length = 20)
    private String phone;
    
    @Column(length = 200)
    private String address;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters, Setters, Constructors
}
```

#### AuditLog.java

```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false, length = 20)
    private String action;  // CREATE, READ, UPDATE, DELETE
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;
    
    @Lob
    @Column(name = "request_body")
    private String requestBody;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "execution_time")
    private Long executionTime;
    
    @Column(name = "client_ip", length = 45)
    private String clientIp;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    // Getters, Setters, Constructors, Builder pattern
}
```

### 5.2 Repository 介面

#### ContactRepository.java

```java
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByNameContaining(String name);
    
    Optional<Contact> findByPhone(String phone);
}
```

#### AuditLogRepository.java

```java
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // 依時間範圍查詢
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // 依操作類型查詢
    List<AuditLog> findByAction(String action);
    
    // 依 API 端點查詢
    List<AuditLog> findByEndpointContaining(String endpoint);
    
    // 依 Client IP 查詢
    List<AuditLog> findByClientIp(String clientIp);
    
    // 組合條件查詢 (使用 JPA Specification 或 @Query)
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:endpoint IS NULL OR a.endpoint LIKE %:endpoint%) AND " +
           "(:clientIp IS NULL OR a.clientIp = :clientIp) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime) " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findByMultipleConditions(
        @Param("action") String action,
        @Param("endpoint") String endpoint,
        @Param("clientIp") String clientIp,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
```

### 5.3 Service 類別

#### ContactService.java

```java
@Service
@Transactional
public class ContactService {
    
    private final ContactRepository contactRepository;
    
    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }
    
    public Contact createContact(ContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setAddress(request.getAddress());
        return contactRepository.save(contact);
    }
    
    @Transactional(readOnly = true)
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
            .orElseThrow(() -> new ContactNotFoundException(id));
    }
    
    public Contact updateContact(Long id, ContactRequest request) {
        Contact contact = getContactById(id);
        if (request.getName() != null) {
            contact.setName(request.getName());
        }
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            contact.setAddress(request.getAddress());
        }
        return contactRepository.save(contact);
    }
    
    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        contactRepository.deleteById(id);
    }
}
```

#### AuditLogService.java

```java
@Service
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    @Async  // 非同步寫入，不影響主流程效能
    public void saveAuditLog(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }
    
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
    
    public AuditLog getAuditLogById(Long id) {
        return auditLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Audit log not found: " + id));
    }
    
    public List<AuditLog> searchAuditLogs(
            String action,
            String endpoint,
            String clientIp,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return auditLogRepository.findByMultipleConditions(
            action, endpoint, clientIp, startTime, endTime);
    }
}
```

### 5.4 Controller 類別

#### ContactController.java

```java
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    
    private final ContactService contactService;
    
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }
    
    @PostMapping
    @Auditable(action = "CREATE")
    public ResponseEntity<ApiResponse<Contact>> createContact(
            @Valid @RequestBody ContactRequest request) {
        Contact contact = contactService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Contact created successfully", contact));
    }
    
    @GetMapping
    @Auditable(action = "READ")
    public ResponseEntity<ApiResponse<List<Contact>>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        return ResponseEntity.ok(ApiResponse.success("Contacts retrieved", contacts));
    }
    
    @GetMapping("/{id}")
    @Auditable(action = "READ")
    public ResponseEntity<ApiResponse<Contact>> getContactById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        return ResponseEntity.ok(ApiResponse.success("Contact found", contact));
    }
    
    @PutMapping("/{id}")
    @Auditable(action = "UPDATE")
    public ResponseEntity<ApiResponse<Contact>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        Contact contact = contactService.updateContact(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contact updated", contact));
    }
    
    @DeleteMapping("/{id}")
    @Auditable(action = "DELETE")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted", null));
    }
}
```

#### AuditLogController.java

```java
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String endpoint,
            @RequestParam(required = false) String clientIp,
            @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<AuditLog> logs;
        
        // 如果有任何查詢條件，使用組合查詢
        if (action != null || endpoint != null || clientIp != null || 
            startTime != null || endTime != null) {
            logs = auditLogService.searchAuditLogs(
                action, endpoint, clientIp, startTime, endTime);
        } else {
            logs = auditLogService.getAllAuditLogs();
        }
        
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLog>> getAuditLogById(@PathVariable Long id) {
        AuditLog log = auditLogService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Audit log found", log));
    }
}
```

---

## 6. AOP 稽核日誌設計

### 6.1 設計概念

使用 Spring AOP 的 `@Around` Advice 攔截所有標註 `@Auditable` 的方法，在方法執行前後收集稽核資訊並寫入資料庫。

```
┌─────────────────────────────────────────────────────────────┐
│                    AOP Audit Flow                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Request ──► AuditLogAspect ──► Controller ──► Service      │
│                    │                │                       │
│                    │                │                       │
│              ┌─────▼─────┐          │                       │
│              │ Before:   │          │                       │
│              │ - Start   │          │                       │
│              │   Timer   │          │                       │
│              │ - Capture │          │                       │
│              │   Request │          │                       │
│              └───────────┘          │                       │
│                                     │                       │
│              ┌───────────┐          │                       │
│              │ After:    │◄─────────┘                       │
│              │ - Stop    │                                  │
│              │   Timer   │                                  │
│              │ - Capture │                                  │
│              │   Response│                                  │
│              │ - Save    │                                  │
│              │   Log     │                                  │
│              └─────┬─────┘                                  │
│                    │                                        │
│                    ▼                                        │
│            AuditLogService.saveAuditLog() [Async]           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 自定義 Annotation

#### Auditable.java

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    
    /**
     * 操作類型：CREATE, READ, UPDATE, DELETE
     */
    String action();
    
    /**
     * 操作描述（選填）
     */
    String description() default "";
}
```

### 6.3 Aspect 實作

#### AuditLogAspect.java

```java
@Aspect
@Component
@Slf4j
public class AuditLogAspect {
    
    private final AuditLogService auditLogService;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;
    
    public AuditLogAspect(AuditLogService auditLogService, 
                          HttpServletRequest request,
                          ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.request = request;
        this.objectMapper = objectMapper;
    }
    
    @Around("@annotation(auditable)")
    public Object auditLog(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        
        // 開始計時
        long startTime = System.currentTimeMillis();
        
        // 取得請求資訊
        String endpoint = request.getRequestURI();
        String httpMethod = request.getMethod();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String requestBody = extractRequestBody(joinPoint);
        
        Object result = null;
        int responseStatus = 200;
        
        try {
            // 執行目標方法
            result = joinPoint.proceed();
            
            // 取得回應狀態碼
            if (result instanceof ResponseEntity) {
                responseStatus = ((ResponseEntity<?>) result).getStatusCode().value();
            }
            
            return result;
            
        } catch (Exception e) {
            responseStatus = 500;
            throw e;
            
        } finally {
            // 計算執行時間
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 建立稽核日誌
            AuditLog auditLog = AuditLog.builder()
                .timestamp(LocalDateTime.now())
                .action(auditable.action())
                .endpoint(endpoint)
                .httpMethod(httpMethod)
                .requestBody(requestBody)
                .responseStatus(responseStatus)
                .executionTime(executionTime)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();
            
            // 非同步儲存（不影響主流程）
            auditLogService.saveAuditLog(auditLog);
            
            log.debug("Audit log recorded: {} {} - {} ms", 
                httpMethod, endpoint, executionTime);
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 處理多個 IP 的情況（取第一個）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    private String extractRequestBody(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg != null && !(arg instanceof HttpServletRequest) 
                    && !(arg instanceof HttpServletResponse)) {
                    // 過濾敏感資訊（如密碼）
                    return objectMapper.writeValueAsString(arg);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract request body", e);
        }
        return null;
    }
}
```

### 6.4 AOP 配置

#### AopConfig.java

```java
@Configuration
@EnableAspectJAutoProxy
@EnableAsync  // 啟用非同步支援
public class AopConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

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

### 12.1 單元測試範圍

| 測試對象 | 測試內容 |
|----------|----------|
| ContactService | CRUD 業務邏輯 |
| AuditLogService | 日誌查詢邏輯 |
| AuditLogAspect | AOP 攔截與日誌記錄 |

### 12.2 整合測試範圍

| 測試對象 | 測試內容 |
|----------|----------|
| ContactController | API 端點、HTTP 狀態碼、回應格式 |
| AuditLogController | 日誌查詢 API |
| End-to-End | 完整流程測試 |

### 12.3 測試案例範例

```java
@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateContact() throws Exception {
        String requestBody = """
            {
                "name": "測試用戶",
                "phone": "0912345678",
                "address": "測試地址"
            }
            """;
            
        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("測試用戶"));
    }
    
    @Test
    void shouldReturnNotFoundForNonExistentContact() throws Exception {
        mockMvc.perform(get("/api/contacts/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }
}
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

### ADR-001: 使用 Spring AOP 實作稽核日誌

**狀態**: 已採用

**背景**: 需要在不侵入業務邏輯的情況下記錄所有 API 呼叫。

**決策**: 使用 Spring AOP 的 @Around advice 搭配自定義 @Auditable annotation。

**理由**:
- 關注點分離：稽核邏輯與業務邏輯完全解耦
- 可維護性：集中管理稽核邏輯
- 可擴展性：易於新增稽核欄位或變更記錄方式
- 低侵入性：不需修改現有 Controller 程式碼

**後果**:
- 需要額外的 AOP 相關知識
- 對於複雜場景可能需要額外配置

### ADR-002: 稽核日誌非同步寫入

**狀態**: 已採用

**背景**: 稽核日誌寫入不應影響主要業務的回應時間。

**決策**: 使用 @Async 進行非同步寫入。

**理由**:
- 效能：不阻塞主要業務流程
- 用戶體驗：維持 API 回應速度

**後果**:
- 日誌寫入可能有些微延遲
- 需要適當處理非同步例外
