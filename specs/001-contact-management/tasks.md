# Tasks: Contact Management System

**Input**: Design documents from `/specs/001-contact-management/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml

**Tests**: This feature specification requires BDD testing with Cucumber. Test tasks are included.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Based on plan.md hexagonal architecture structure:
- **Domain**: `src/main/java/com/example/contact/domain/`
- **Application**: `src/main/java/com/example/contact/application/`
- **Infrastructure**: `src/main/java/com/example/contact/infrastructure/`
- **Tests**: `src/test/java/com/example/contact/`
- **Cucumber Features**: `src/test/resources/features/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create Gradle project with build.gradle.kts per research.md configuration
- [x] T002 [P] Create directory structure per plan.md hexagonal architecture in src/main/java/com/example/contact/
- [x] T003 [P] Create test directory structure in src/test/java/com/example/contact/ (unit/, integration/, contract/)
- [x] T004 [P] Configure application.yml with shared settings in src/main/resources/application.yml
- [x] T005 [P] Configure application-dev.yml for H2 database in src/main/resources/application-dev.yml
- [x] T006 [P] Configure application-prod.yml for PostgreSQL in src/main/resources/application-prod.yml
- [x] T007 Create Spring Boot main application class in src/main/java/com/example/contact/ContactApplication.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

### Domain Layer Foundation

- [x] T008 [P] Create ContactId value object in src/main/java/com/example/contact/domain/model/ContactId.java
- [x] T009 [P] Create Contact domain entity in src/main/java/com/example/contact/domain/model/Contact.java
- [x] T010 [P] Create OperationType enum in src/main/java/com/example/contact/domain/model/OperationType.java
- [x] T011 [P] Create AuditLog domain entity in src/main/java/com/example/contact/domain/model/AuditLog.java
- [x] T012 [P] Create ContactNotFoundException in src/main/java/com/example/contact/domain/exception/ContactNotFoundException.java
- [x] T013 [P] Create ValidationException in src/main/java/com/example/contact/domain/exception/ValidationException.java

### Application Layer Foundation (Ports)

- [x] T014 [P] Create ContactRepository port (Output) in src/main/java/com/example/contact/application/port/out/ContactRepository.java
- [x] T015 [P] Create AuditLogRepository port (Output) in src/main/java/com/example/contact/application/port/out/AuditLogRepository.java

### Infrastructure Layer Foundation

- [x] T016 [P] Create ContactJpaEntity in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/entity/ContactJpaEntity.java
- [x] T017 [P] Create AuditLogJpaEntity in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/entity/AuditLogJpaEntity.java
- [x] T018 [P] Create ContactJpaRepository (Spring Data) in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/ContactJpaRepository.java
- [x] T019 [P] Create AuditLogJpaRepository (Spring Data) in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/AuditLogJpaRepository.java
- [x] T020 [P] Create ContactMapper (Domain <-> JPA) in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/ContactMapper.java
- [x] T021 [P] Create AuditLogMapper (Domain <-> JPA) in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/AuditLogMapper.java
- [x] T022 Create ContactJpaAdapter implementing ContactRepository in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/ContactJpaAdapter.java
- [x] T023 Create AuditLogJpaAdapter implementing AuditLogRepository in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/AuditLogJpaAdapter.java

### Infrastructure Config

- [x] T024 [P] Create WebConfig in src/main/java/com/example/contact/infrastructure/config/WebConfig.java
- [x] T025 [P] Create PersistenceConfig in src/main/java/com/example/contact/infrastructure/config/PersistenceConfig.java

### Common DTOs and Error Handling

- [x] T026 [P] Create ErrorResponse DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/ErrorResponse.java
- [x] T027 [P] Create ValidationError DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/ValidationError.java
- [x] T028 Create GlobalExceptionHandler in src/main/java/com/example/contact/infrastructure/adapter/in/web/GlobalExceptionHandler.java

### Test Foundation

- [x] T029 [P] Create Cucumber configuration class in src/test/java/com/example/contact/CucumberSpringConfiguration.java
- [x] T030 [P] Create Cucumber test runner in src/test/java/com/example/contact/CucumberTest.java
- [x] T031 Create test application.yml in src/test/resources/application.yml

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 新增聯絡人 (Priority: P1) MVP

**Goal**: 使用者可以建立新的聯絡人記錄，包含姓名、電話、地址

**Independent Test**: POST /api/contacts with valid data returns 201 with generated ID

### Tests for User Story 1

- [x] T032 [P] [US1] Create contact-create.feature in src/test/resources/features/contact-create.feature
- [x] T033 [P] [US1] Create unit test for Contact domain entity in src/test/java/com/example/contact/unit/domain/ContactTest.java
- [x] T034 [P] [US1] Create unit test for ContactService in src/test/java/com/example/contact/unit/application/ContactServiceTest.java

### Implementation for User Story 1

- [x] T035 [P] [US1] Create CreateContactCommand in src/main/java/com/example/contact/application/port/in/CreateContactCommand.java
- [x] T036 [P] [US1] Create CreateContactUseCase port (Input) in src/main/java/com/example/contact/application/port/in/CreateContactUseCase.java
- [x] T037 [P] [US1] Create CreateContactRequest DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/CreateContactRequest.java
- [x] T038 [P] [US1] Create ContactResponse DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/ContactResponse.java
- [x] T039 [US1] Implement ContactService.createContact in src/main/java/com/example/contact/application/service/ContactService.java
- [x] T040 [US1] Create ContactController with POST /api/contacts in src/main/java/com/example/contact/infrastructure/adapter/in/web/ContactController.java
- [x] T041 [US1] Create ContactStepDefinitions for create scenarios in src/test/java/com/example/contact/integration/ContactStepDefinitions.java

**Checkpoint**: User Story 1 (新增聯絡人) is fully functional and testable independently

---

## Phase 4: User Story 2 - 查詢聯絡人 (Priority: P1) MVP

**Goal**: 使用者可以查詢單一聯絡人或所有聯絡人列表

**Independent Test**: GET /api/contacts returns list; GET /api/contacts/{id} returns single contact

### Tests for User Story 2

- [x] T042 [P] [US2] Create contact-query.feature in src/test/resources/features/contact-query.feature
- [x] T043 [P] [US2] Add unit tests for GetContactUseCase in src/test/java/com/example/contact/unit/application/ContactServiceTest.java

### Implementation for User Story 2

- [x] T044 [P] [US2] Create GetContactUseCase port (Input) in src/main/java/com/example/contact/application/port/in/GetContactUseCase.java
- [x] T045 [US2] Implement ContactService.getContactById and getAllContacts in src/main/java/com/example/contact/application/service/ContactService.java
- [x] T046 [US2] Add GET /api/contacts and GET /api/contacts/{id} to ContactController in src/main/java/com/example/contact/infrastructure/adapter/in/web/ContactController.java
- [x] T047 [US2] Add query step definitions to ContactStepDefinitions in src/test/java/com/example/contact/integration/ContactStepDefinitions.java

**Checkpoint**: User Stories 1 AND 2 should both work independently (MVP Complete)

---

## Phase 5: User Story 3 - 修改聯絡人 (Priority: P2)

**Goal**: 使用者可以更新現有聯絡人的資料

**Independent Test**: PUT /api/contacts/{id} with valid data updates contact and returns updated data

### Tests for User Story 3

- [x] T048 [P] [US3] Create contact-update.feature in src/test/resources/features/contact-update.feature
- [x] T049 [P] [US3] Add unit tests for UpdateContactUseCase in src/test/java/com/example/contact/unit/application/ContactServiceTest.java

### Implementation for User Story 3

- [x] T050 [P] [US3] Create UpdateContactCommand in src/main/java/com/example/contact/application/port/in/UpdateContactCommand.java
- [x] T051 [P] [US3] Create UpdateContactUseCase port (Input) in src/main/java/com/example/contact/application/port/in/UpdateContactUseCase.java
- [x] T052 [P] [US3] Create UpdateContactRequest DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/UpdateContactRequest.java
- [x] T053 [US3] Implement ContactService.updateContact in src/main/java/com/example/contact/application/service/ContactService.java
- [x] T054 [US3] Add PUT /api/contacts/{id} to ContactController in src/main/java/com/example/contact/infrastructure/adapter/in/web/ContactController.java
- [x] T055 [US3] Add update step definitions to ContactStepDefinitions in src/test/java/com/example/contact/integration/ContactStepDefinitions.java

**Checkpoint**: User Story 3 (修改聯絡人) is fully functional

---

## Phase 6: User Story 4 - 刪除聯絡人 (Priority: P2)

**Goal**: 使用者可以刪除不再需要的聯絡人記錄

**Independent Test**: DELETE /api/contacts/{id} removes contact and returns 204

### Tests for User Story 4

- [x] T056 [P] [US4] Create contact-delete.feature in src/test/resources/features/contact-delete.feature
- [x] T057 [P] [US4] Add unit tests for DeleteContactUseCase in src/test/java/com/example/contact/unit/application/ContactServiceTest.java

### Implementation for User Story 4

- [x] T058 [P] [US4] Create DeleteContactUseCase port (Input) in src/main/java/com/example/contact/application/port/in/DeleteContactUseCase.java
- [x] T059 [US4] Implement ContactService.deleteContact in src/main/java/com/example/contact/application/service/ContactService.java
- [x] T060 [US4] Add DELETE /api/contacts/{id} to ContactController in src/main/java/com/example/contact/infrastructure/adapter/in/web/ContactController.java
- [x] T061 [US4] Add delete step definitions to ContactStepDefinitions in src/test/java/com/example/contact/integration/ContactStepDefinitions.java

**Checkpoint**: User Story 4 (刪除聯絡人) is fully functional - Full CRUD Complete

---

## Phase 7: User Story 5 - 稽核日誌自動記錄 (Priority: P2)

**Goal**: 所有 API 操作自動記錄稽核日誌，非同步處理不影響回應時間

**Independent Test**: 執行任意 Contact API 後，稽核日誌已記錄且包含完整資訊

### Tests for User Story 5

- [x] T062 [P] [US5] Create audit-log.feature for auto-recording in src/test/resources/features/audit-log.feature
- [x] T063 [P] [US5] Create unit test for AuditLogService in src/test/java/com/example/contact/unit/application/AuditLogServiceTest.java
- [x] T064 [P] [US5] Create unit test for AuditLogAspect in src/test/java/com/example/contact/unit/infrastructure/AuditLogAspectTest.java

### Implementation for User Story 5

- [x] T065 [P] [US5] Create RecordAuditLogCommand in src/main/java/com/example/contact/application/port/in/RecordAuditLogCommand.java
- [x] T066 [P] [US5] Create RecordAuditLogUseCase port (Input) in src/main/java/com/example/contact/application/port/in/RecordAuditLogUseCase.java
- [x] T067 [P] [US5] Create AsyncConfig with @EnableAsync in src/main/java/com/example/contact/infrastructure/config/AsyncConfig.java
- [x] T068 [US5] Implement AuditLogService.recordAuditLog in src/main/java/com/example/contact/application/service/AuditLogService.java
- [x] T069 [US5] Create AuditLogAspect with @Around advice in src/main/java/com/example/contact/infrastructure/aop/AuditLogAspect.java
- [x] T070 [US5] Add audit log step definitions in src/test/java/com/example/contact/integration/AuditLogStepDefinitions.java

**Checkpoint**: User Story 5 (稽核日誌自動記錄) is fully functional

---

## Phase 8: User Story 6 - 稽核日誌查詢 (Priority: P3)

**Goal**: 管理員可以依據各種條件查詢稽核日誌

**Independent Test**: GET /api/audit-logs with filters returns matching logs with pagination

### Tests for User Story 6

- [x] T071 [P] [US6] Add audit-log-query scenarios to audit-log.feature in src/test/resources/features/audit-log.feature
- [x] T072 [P] [US6] Add unit tests for QueryAuditLogUseCase in src/test/java/com/example/contact/unit/application/AuditLogServiceTest.java

### Implementation for User Story 6

- [x] T073 [P] [US6] Create AuditLogQueryCriteria in src/main/java/com/example/contact/application/port/in/AuditLogQueryCriteria.java
- [x] T074 [P] [US6] Create QueryAuditLogUseCase port (Input) in src/main/java/com/example/contact/application/port/in/QueryAuditLogUseCase.java
- [x] T075 [P] [US6] Create AuditLogResponse DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/AuditLogResponse.java
- [x] T076 [P] [US6] Create AuditLogPageResponse DTO in src/main/java/com/example/contact/infrastructure/adapter/in/web/dto/AuditLogPageResponse.java
- [x] T077 [US6] Extend AuditLogRepository with query methods in src/main/java/com/example/contact/application/port/out/AuditLogRepository.java
- [x] T078 [US6] Implement AuditLogJpaAdapter query methods with Specification in src/main/java/com/example/contact/infrastructure/adapter/out/persistence/AuditLogJpaAdapter.java
- [x] T079 [US6] Implement AuditLogService.queryAuditLogs in src/main/java/com/example/contact/application/service/AuditLogService.java
- [x] T080 [US6] Create AuditLogController with GET /api/audit-logs in src/main/java/com/example/contact/infrastructure/adapter/in/web/AuditLogController.java
- [x] T081 [US6] Add query step definitions to AuditLogStepDefinitions in src/test/java/com/example/contact/integration/AuditLogStepDefinitions.java

**Checkpoint**: User Story 6 (稽核日誌查詢) is fully functional

---

## Phase 9: User Story 7 - 資料庫管理介面 (Priority: P3)

**Goal**: 開發人員可透過 H2 Console 查看資料庫內容

**Independent Test**: Access /h2-console and execute SQL queries successfully

### Implementation for User Story 7

- [x] T082 [US7] Verify H2 console configuration in application-dev.yml (already configured in T005)
- [x] T083 [US7] Add H2 console security config if needed in src/main/java/com/example/contact/infrastructure/config/WebConfig.java
- [x] T084 [US7] Create manual test documentation for H2 console access in specs/001-contact-management/quickstart.md

**Checkpoint**: User Story 7 (資料庫管理介面) is fully functional

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T085 [P] Run all Cucumber tests and verify all scenarios pass
- [x] T086 [P] Run JaCoCo coverage verification (>= 80% for domain/application layers)
- [x] T087 [P] Add Checkstyle configuration in config/checkstyle/checkstyle.xml
- [x] T088 Run full build and fix any issues: ./gradlew build
- [x] T089 Validate quickstart.md by following setup instructions end-to-end
- [x] T090 Final code review for hexagonal architecture compliance

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1: Setup → Phase 2: Foundational → Phases 3-9: User Stories → Phase 10: Polish
```

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - **BLOCKS all user stories**
- **User Stories (Phases 3-9)**: All depend on Foundational phase completion
- **Polish (Phase 10)**: Depends on all user stories being complete

### User Story Dependencies

| User Story | Priority | Depends On | Can Run In Parallel With |
|------------|----------|------------|-------------------------|
| US1: 新增聯絡人 | P1 | Foundational | US2 |
| US2: 查詢聯絡人 | P1 | Foundational | US1 |
| US3: 修改聯絡人 | P2 | Foundational | US4, US5 |
| US4: 刪除聯絡人 | P2 | Foundational | US3, US5 |
| US5: 稽核日誌自動記錄 | P2 | Foundational | US3, US4 |
| US6: 稽核日誌查詢 | P3 | US5 (needs audit logs exist) | US7 |
| US7: 資料庫管理介面 | P3 | Foundational | US6 |

### Within Each User Story (TDD Order)

1. **Tests FIRST** - Write feature file and unit tests (should FAIL)
2. **Models/Commands** - Create data structures
3. **Ports** - Define Use Case interfaces
4. **Services** - Implement business logic
5. **Controllers** - Expose REST endpoints
6. **Step Definitions** - Wire up Cucumber tests (should now PASS)

### Parallel Opportunities

**Phase 1 (Setup)**: T002-T006 can run in parallel

**Phase 2 (Foundational)**:
- T008-T013 (Domain entities) in parallel
- T014-T015 (Ports) in parallel
- T016-T021 (JPA entities and mappers) in parallel
- T024-T027 (Config and DTOs) in parallel

**User Story Phases**: Once Foundational completes:
- US1 and US2 can run in parallel (both P1)
- US3, US4, US5 can run in parallel (all P2)
- US6 and US7 can run in parallel (both P3, but US6 needs US5)

---

## Parallel Example: User Story 1

```bash
# All tests for US1 together:
T032: contact-create.feature
T033: Contact domain unit test
T034: ContactService unit test

# All DTOs/Commands for US1 together:
T035: CreateContactCommand
T036: CreateContactUseCase
T037: CreateContactRequest DTO
T038: ContactResponse DTO

# Sequential after above:
T039: ContactService.createContact implementation
T040: ContactController POST endpoint
T041: Step definitions
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (**CRITICAL** - blocks all stories)
3. Complete Phase 3: User Story 1 (新增聯絡人)
4. Complete Phase 4: User Story 2 (查詢聯絡人)
5. **STOP and VALIDATE**: Test both stories independently
6. Deploy/demo if ready - **MVP COMPLETE** (Create + Read)

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. Add US1 + US2 → Test → **MVP** (Create + Read)
3. Add US3 + US4 → Test → **Full CRUD**
4. Add US5 → Test → **Audit Logging**
5. Add US6 + US7 → Test → **Complete Feature**

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: US1 (新增聯絡人) + US3 (修改聯絡人)
   - Developer B: US2 (查詢聯絡人) + US4 (刪除聯絡人)
   - Developer C: US5 (稽核日誌自動記錄) → US6 (稽核日誌查詢)
3. Stories complete and integrate independently

---

## Summary

| Phase | Tasks | User Story | Priority |
|-------|-------|------------|----------|
| Phase 1 | T001-T007 (7) | Setup | - |
| Phase 2 | T008-T031 (24) | Foundational | - |
| Phase 3 | T032-T041 (10) | US1: 新增聯絡人 | P1 |
| Phase 4 | T042-T047 (6) | US2: 查詢聯絡人 | P1 |
| Phase 5 | T048-T055 (8) | US3: 修改聯絡人 | P2 |
| Phase 6 | T056-T061 (6) | US4: 刪除聯絡人 | P2 |
| Phase 7 | T062-T070 (9) | US5: 稽核日誌自動記錄 | P2 |
| Phase 8 | T071-T081 (11) | US6: 稽核日誌查詢 | P3 |
| Phase 9 | T082-T084 (3) | US7: 資料庫管理介面 | P3 |
| Phase 10 | T085-T090 (6) | Polish | - |
| **Total** | **90 tasks** | **7 User Stories** | |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing (TDD)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Follow hexagonal architecture: Domain → Application → Infrastructure
