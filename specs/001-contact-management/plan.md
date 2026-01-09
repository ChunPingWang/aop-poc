# Implementation Plan: Contact Management System

**Branch**: `001-contact-management` | **Date**: 2026-01-10 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-contact-management/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

實作一個聯絡人管理系統，提供完整的 CRUD API 功能（新增、查詢、修改、刪除聯絡人），並整合 AOP 稽核日誌自動記錄機制。系統採用六角形架構（Hexagonal Architecture）確保領域邏輯與框架分離，使用 Spring Boot 3 作為基礎框架，搭配 Cucumber 進行 BDD 驗收測試。

## Technical Context

**Language/Version**: Java 17
**Build Tool**: Gradle
**Primary Dependencies**: Spring Boot 3, Spring Web, Spring Data JPA, Spring AOP
**Storage**: H2 (開發環境), PostgreSQL (正式環境)
**Testing**: JUnit 5, Cucumber (BDD), Mockito
**Target Platform**: JVM Server (Linux/macOS/Windows)
**Project Type**: Single backend API application
**Architecture**: Hexagonal Architecture (Ports & Adapters)
**Performance Goals**: 平均回應時間 < 200ms, 單一查詢 < 1s, 列表查詢 < 2s
**Constraints**: 稽核日誌非同步處理，對主要操作延遲影響 < 10%
**Scale/Scope**: 開發/測試環境，單一部署，無認證需求

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Verification | Pre-Design | Post-Design |
|-----------|--------------|------------|-------------|
| I. Hexagonal Architecture | Domain layer (Contact, AuditLog entities) 無框架依賴？Ports 定義於 application layer？Adapters (JPA, REST) 在 infrastructure layer？ | ☑ 已規劃 | ☑ data-model.md 定義純 Domain Entity，JPA Entity 在 infrastructure |
| II. Domain-Driven Design | 使用領域術語（聯絡人、稽核日誌）？Contact 為獨立 Aggregate？ContactRepository 介面定義於 application layer？ | ☑ 已規劃 | ☑ Contact Aggregate + ContactId Value Object 已定義 |
| III. SOLID Principles | ContactService、AuditLogService 單一職責？透過 Port 介面注入依賴？ | ☑ 已規劃 | ☑ UseCase 介面定義於 application/port/in/，依賴注入設計完成 |
| IV. Test-Driven Development | 先寫測試再實作？遵循 Red-Green-Refactor？單元測試 < 100ms？ | ☑ 已規劃 | ☑ 測試結構規劃於 quickstart.md，coverage 門檻 80% |
| V. Behavior-Driven Development | Acceptance criteria 使用 Given/When/Then？Cucumber scenarios 可執行？ | ☑ 已規劃（spec.md 已定義） | ☑ Cucumber 整合配置於 research.md，feature files 結構已規劃 |
| VI. Code Quality Standards | Checkstyle/SpotBugs 配置？Domain/Application layer 覆蓋率 >= 80%？循環複雜度 <= 10？ | ☑ 已規劃 | ☑ JaCoCo 配置於 build.gradle.kts，門檻 80% |

## Project Structure

### Documentation (this feature)

```text
specs/001-contact-management/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── openapi.yaml     # REST API contract
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/com/example/contact/
│   │   ├── domain/                      # Domain Layer (innermost)
│   │   │   ├── model/                   # Entities, Value Objects
│   │   │   │   ├── Contact.java         # Contact aggregate root
│   │   │   │   ├── ContactId.java       # Contact ID value object
│   │   │   │   └── AuditLog.java        # AuditLog entity
│   │   │   ├── service/                 # Domain services (if needed)
│   │   │   └── event/                   # Domain events
│   │   │
│   │   ├── application/                 # Application Layer
│   │   │   ├── port/
│   │   │   │   ├── in/                  # Input ports (use cases)
│   │   │   │   │   ├── CreateContactUseCase.java
│   │   │   │   │   ├── GetContactUseCase.java
│   │   │   │   │   ├── UpdateContactUseCase.java
│   │   │   │   │   ├── DeleteContactUseCase.java
│   │   │   │   │   ├── QueryAuditLogUseCase.java
│   │   │   │   │   └── RecordAuditLogUseCase.java
│   │   │   │   └── out/                 # Output ports (repository interfaces)
│   │   │   │       ├── ContactRepository.java
│   │   │   │       └── AuditLogRepository.java
│   │   │   └── service/                 # Application services (use case implementations)
│   │   │       ├── ContactService.java
│   │   │       └── AuditLogService.java
│   │   │
│   │   └── infrastructure/              # Infrastructure Layer (outermost)
│   │       ├── adapter/
│   │       │   ├── in/
│   │       │   │   └── web/             # REST controllers (driving adapters)
│   │       │   │       ├── ContactController.java
│   │       │   │       ├── AuditLogController.java
│   │       │   │       └── dto/         # Request/Response DTOs
│   │       │   └── out/
│   │       │       └── persistence/     # JPA adapters (driven adapters)
│   │       │           ├── ContactJpaAdapter.java
│   │       │           ├── AuditLogJpaAdapter.java
│   │       │           └── entity/      # JPA entities
│   │       ├── aop/                     # AOP aspects for audit logging
│   │       │   └── AuditLogAspect.java
│   │       └── config/                  # Spring configuration
│   │           ├── WebConfig.java
│   │           ├── PersistenceConfig.java
│   │           └── AsyncConfig.java
│   │
│   └── resources/
│       ├── application.yml              # Main config
│       ├── application-dev.yml          # H2 config
│       └── application-prod.yml         # PostgreSQL config
│
└── test/
    ├── java/com/example/contact/
    │   ├── unit/                        # Unit tests (domain + application)
    │   ├── integration/                 # Integration tests (adapters)
    │   └── contract/                    # Contract tests (port compliance)
    └── resources/
        └── features/                    # Cucumber feature files
            ├── contact-create.feature
            ├── contact-query.feature
            ├── contact-update.feature
            ├── contact-delete.feature
            └── audit-log.feature
```

**Structure Decision**: 採用六角形架構（Hexagonal Architecture）的標準 Java 專案結構。Domain layer 包含純 Java 實體，無任何框架依賴。Application layer 定義 Port 介面和 Use Case 實作。Infrastructure layer 包含所有 Spring Boot、JPA 和 Web 相關的 Adapter 實作。測試依據測試金字塔原則組織為 unit、integration 和 contract 三層，Cucumber BDD 測試檔案放置於 test/resources/features/。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

本設計無任何憲章違規，所有原則均已滿足。

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| (None) | - | - |

---

## Generated Artifacts

本 `/speckit.plan` 執行產生以下文件：

| Artifact | Path | Description |
|----------|------|-------------|
| Implementation Plan | `specs/001-contact-management/plan.md` | 本文件 |
| Research | `specs/001-contact-management/research.md` | 技術決策與研究結果 |
| Data Model | `specs/001-contact-management/data-model.md` | Entity 定義與資料庫結構 |
| API Contract | `specs/001-contact-management/contracts/openapi.yaml` | OpenAPI 3.0 規格 |
| Quickstart | `specs/001-contact-management/quickstart.md` | 專案啟動指南 |
| Agent Context | `CLAUDE.md` | Claude Code agent context (已更新) |

---

## Next Steps

1. **Review artifacts**: 檢視上述產出文件
2. **Run `/speckit.tasks`**: 產生實作任務清單 (`tasks.md`)
3. **Run `/speckit.implement`**: 執行實作任務
