# Research: Contact Management System

**Feature Branch**: `001-contact-management`
**Date**: 2026-01-10

## Executive Summary

本文件記錄 Contact Management System 實作所需的技術研究結果，涵蓋 Spring Boot 3 六角形架構實作、資料庫配置策略、BDD 測試整合、以及 AOP 稽核日誌設計等關鍵技術決策。

---

## 1. Hexagonal Architecture with Spring Boot 3

### Decision
採用標準六角形架構（Ports & Adapters）模式，將程式碼分為三層：Domain、Application、Infrastructure。

### Rationale
- **Domain Layer**: 純 Java 實體，無任何 Spring/JPA 註解，確保領域邏輯可獨立測試
- **Application Layer**: 定義 Use Case 介面（Input Ports）和 Repository 介面（Output Ports）
- **Infrastructure Layer**: 實作所有 Spring Boot、JPA、REST 相關的 Adapters

### Implementation Details

```
依賴方向：Infrastructure → Application → Domain
禁止方向：Domain → Application → Infrastructure
```

**Port 定義範例**:
```java
// Application Layer - Input Port
public interface CreateContactUseCase {
    Contact createContact(CreateContactCommand command);
}

// Application Layer - Output Port
public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(ContactId id);
    List<Contact> findAll();
    void deleteById(ContactId id);
}
```

**Adapter 實作範例**:
```java
// Infrastructure Layer - Driven Adapter
@Repository
public class ContactJpaAdapter implements ContactRepository {
    private final ContactJpaRepository jpaRepository;

    // 轉換 Domain Entity <-> JPA Entity
}
```

### Alternatives Considered
1. **分層架構（Layered Architecture）**: 較簡單但 Domain 與 Infrastructure 耦合度高，測試困難
2. **Clean Architecture**: 類似但更複雜，對本專案規模過於繁重

---

## 2. Database Configuration Strategy

### Decision
使用 Spring Profiles 切換 H2（開發）和 PostgreSQL（正式）環境。

### Rationale
- H2 in-memory 模式適合開發和測試，啟動快速且無需外部依賴
- PostgreSQL 提供正式環境所需的穩定性和效能
- Profile-based 配置讓環境切換無需改動程式碼

### Implementation Details

**application.yml (共用配置)**:
```yaml
spring:
  application:
    name: contact-management
  jpa:
    open-in-view: false
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
```

**application-dev.yml (H2)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:contactdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

**application-prod.yml (PostgreSQL)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:contactdb}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Alternatives Considered
1. **Testcontainers**: 開發時使用真實 PostgreSQL 容器，更接近正式環境但啟動慢
2. **單一 H2 配置**: 簡單但無法驗證 PostgreSQL 特定行為

---

## 3. Cucumber BDD Integration

### Decision
使用 `cucumber-spring` 整合 Cucumber 與 Spring Boot，透過 `@CucumberContextConfiguration` 載入測試 context。

### Rationale
- Cucumber 提供 Gherkin 語法實現可執行的驗收測試
- 與 Spring Boot 整合可直接注入 Service 和 Repository 進行測試
- 符合憲章要求的 BDD 原則

### Implementation Details

**Gradle 依賴**:
```groovy
testImplementation 'io.cucumber:cucumber-java:7.15.0'
testImplementation 'io.cucumber:cucumber-spring:7.15.0'
testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.15.0'
```

**Cucumber 配置類**:
```java
package com.example.contact;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {
}
```

**Step Definitions 範例**:
```java
public class ContactStepDefinitions {
    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<ContactResponse> response;

    @Given("系統正常運作且無任何聯絡人")
    public void systemIsRunningWithNoContacts() {
        // 清理資料庫
    }

    @When("使用者提供姓名{string}、電話{string}發送新增請求")
    public void userCreatesContact(String name, String phone) {
        CreateContactRequest request = new CreateContactRequest(name, phone, null);
        response = restTemplate.postForEntity("/api/contacts", request, ContactResponse.class);
    }

    @Then("系統回傳包含系統產生 ID 的完整聯絡人資訊")
    public void systemReturnsContactWithId() {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
    }
}
```

### Alternatives Considered
1. **純 JUnit Integration Test**: 較簡單但缺乏 BDD 可讀性
2. **Karate**: 功能強大但學習曲線較陡，團隊不熟悉

---

## 4. AOP Audit Logging

### Decision
使用 Spring AOP `@Around` advice 攔截 REST Controller 方法，非同步記錄稽核日誌。

### Rationale
- AOP 可透明地橫切所有 API 端點，無需修改業務程式碼
- 非同步執行避免影響主要請求的回應時間（目標：延遲影響 < 10%）
- 集中管理稽核邏輯，符合 SRP 原則

### Implementation Details

**Async 配置**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public AsyncTaskExecutor auditLogExecutor(SimpleAsyncTaskExecutorBuilder builder) {
        return builder
            .threadNamePrefix("audit-")
            .virtualThreads(true)  // Java 21+ 可用虛擬執行緒
            .build();
    }
}
```

**AOP Aspect**:
```java
@Aspect
@Component
public class AuditLogAspect {
    private final RecordAuditLogUseCase recordAuditLogUseCase;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getCurrentRequest();

        Object result;
        int statusCode;
        try {
            result = joinPoint.proceed();
            statusCode = 200; // 或從 ResponseEntity 取得
        } catch (Exception e) {
            statusCode = 500;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            recordAuditLogAsync(request, statusCode, executionTime);
        }
        return result;
    }

    @Async("auditLogExecutor")
    public void recordAuditLogAsync(HttpServletRequest request, int statusCode, long executionTime) {
        // 記錄稽核日誌
    }
}
```

**稽核日誌欄位**:
- `operationTime`: 操作時間戳
- `operationType`: CREATE/READ/UPDATE/DELETE
- `apiEndpoint`: 請求路徑
- `httpMethod`: GET/POST/PUT/DELETE
- `requestBody`: 請求內容（敏感資料遮罩）
- `responseStatus`: HTTP 狀態碼
- `executionTimeMs`: 執行時間（毫秒）
- `clientIp`: 用戶端 IP
- `userAgent`: 用戶代理

### Alternatives Considered
1. **Filter/Interceptor**: 較底層但難以取得方法層級資訊
2. **手動在每個 Controller 方法記錄**: 違反 DRY，維護成本高
3. **Spring Boot Actuator**: 內建但不支援自訂稽核日誌結構

---

## 5. Gradle Build Configuration

### Decision
使用 Gradle Kotlin DSL 配置專案，整合 JaCoCo 覆蓋率報告和 Checkstyle。

### Rationale
- Kotlin DSL 提供更好的 IDE 支援和型別安全
- JaCoCo 驗證測試覆蓋率符合憲章要求（>= 80%）
- Checkstyle 確保程式碼風格一致

### Implementation Details

**build.gradle.kts**:
```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
    checkstyle
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.cucumber:cucumber-java:7.15.0")
    testImplementation("io.cucumber:cucumber-spring:7.15.0")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.15.0")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}
```

### Alternatives Considered
1. **Maven**: 較成熟但配置冗長，團隊偏好 Gradle
2. **Gradle Groovy DSL**: 較鬆散，缺乏型別安全

---

## 6. Domain Model Design

### Decision
- `Contact` 作為 Aggregate Root，使用 Value Object `ContactId` 作為識別碼
- `AuditLog` 作為獨立 Entity（非 Aggregate，因無複雜業務邏輯）
- Domain Entity 完全獨立於 JPA Entity

### Rationale
- Value Object 提供型別安全，避免 ID 混淆
- 分離 Domain/JPA Entity 確保領域純淨性
- AuditLog 簡化為 Entity 因其僅作為記錄用途，無需 DDD 完整建模

### Implementation Details

**Domain Entity**:
```java
// Domain Layer - 無任何框架依賴
public class Contact {
    private final ContactId id;
    private String name;
    private String phone;
    private String address;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 建構子和行為方法
    public void updateInfo(String name, String phone, String address) {
        // 驗證邏輯
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }
}

public record ContactId(Long value) {
    public ContactId {
        Objects.requireNonNull(value, "Contact ID cannot be null");
    }
}
```

**JPA Entity（Infrastructure Layer）**:
```java
@Entity
@Table(name = "contacts")
public class ContactJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA 需要的 getter/setter
}
```

### Alternatives Considered
1. **共用 Domain/JPA Entity**: 較簡單但違反六角形架構原則
2. **使用 UUID 作為 ID**: 分散式友好但本專案單機部署，Long 足夠

---

## Summary of Technology Decisions

| 決策項目 | 選擇 | 主要原因 |
|---------|------|---------|
| 架構模式 | Hexagonal Architecture | 符合憲章要求，領域邏輯可獨立測試 |
| 建置工具 | Gradle Kotlin DSL | 型別安全，IDE 支援佳 |
| 測試框架 | JUnit 5 + Cucumber | BDD 原則，可執行驗收測試 |
| 資料庫切換 | Spring Profiles | 環境隔離，配置簡潔 |
| 稽核日誌 | Spring AOP + @Async | 非侵入式，非同步不阻塞 |
| 領域建模 | Aggregate + Value Object | DDD 原則，型別安全 |

---

## Open Questions (Resolved)

All technical decisions have been made. No outstanding clarifications needed.
