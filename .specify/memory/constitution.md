<!--
  ============================================================================
  SYNC IMPACT REPORT
  ============================================================================
  Version Change: 0.0.0 → 1.0.0 (MAJOR - initial constitution establishment)

  Added Principles:
  - I. Hexagonal Architecture (Ports & Adapters)
  - II. Domain-Driven Design (DDD)
  - III. SOLID Principles
  - IV. Test-Driven Development (TDD)
  - V. Behavior-Driven Development (BDD)
  - VI. Code Quality Standards

  Added Sections:
  - Architectural Constraints
  - Testing Standards
  - Governance

  Templates Status:
  - .specify/templates/plan-template.md: ⚠ pending (Constitution Check section
    references generic gates - will auto-reference this constitution)
  - .specify/templates/spec-template.md: ✅ compatible (BDD scenarios align with
    Given/When/Then format already in template)
  - .specify/templates/tasks-template.md: ✅ compatible (test-first tasks align
    with TDD principle)

  Deferred Items: None
  ============================================================================
-->

# AOP-POC Constitution

## Core Principles

### I. Hexagonal Architecture (Ports & Adapters)

The system MUST follow Hexagonal Architecture (Ports & Adapters) pattern:

- **Domain Layer (Core)**: Contains business logic, domain entities, and use cases.
  MUST NOT depend on any external framework, database, or infrastructure.
- **Application Layer**: Orchestrates use cases and defines ports (interfaces).
  MUST NOT contain business rules or infrastructure details.
- **Infrastructure Layer (Outer)**: Contains all framework integrations, database
  implementations, external APIs, and adapters. All frameworks MUST reside here.
- **Dependency Rule**: Inner layers MUST NOT reference outer layers directly.
  Inner layers access outer layers ONLY through ports (interfaces) defined in the
  application layer.

**Rationale**: Enables testability, framework independence, and clear separation of
concerns. The domain remains pure and technology-agnostic.

### II. Domain-Driven Design (DDD)

All feature development MUST apply Domain-Driven Design principles:

- **Ubiquitous Language**: Code MUST use domain terminology consistently. Variable
  names, class names, and methods MUST reflect business concepts.
- **Bounded Contexts**: Each module MUST have clearly defined boundaries.
  Cross-context communication MUST use explicit contracts.
- **Aggregates**: Related entities MUST be grouped into aggregates with a single
  aggregate root controlling all modifications.
- **Value Objects**: Immutable domain concepts MUST be modeled as value objects.
- **Domain Events**: State changes with business significance MUST emit domain events.
- **Repository Pattern**: Data access MUST be abstracted through repository interfaces
  defined in the domain/application layer, implemented in infrastructure.

**Rationale**: Aligns code structure with business domain, improving maintainability
and communication between developers and domain experts.

### III. SOLID Principles

All code MUST adhere to SOLID principles:

- **Single Responsibility (SRP)**: Each class/module MUST have exactly one reason to
  change. Classes exceeding 200 lines MUST be reviewed for SRP violations.
- **Open/Closed (OCP)**: Modules MUST be open for extension but closed for modification.
  New behavior MUST be addable without changing existing code.
- **Liskov Substitution (LSP)**: Subtypes MUST be substitutable for their base types
  without altering program correctness.
- **Interface Segregation (ISP)**: Clients MUST NOT be forced to depend on interfaces
  they do not use. Prefer many small interfaces over few large ones.
- **Dependency Inversion (DIP)**: High-level modules MUST NOT depend on low-level
  modules. Both MUST depend on abstractions (interfaces/ports).

**Rationale**: Produces maintainable, extensible, and testable code that adapts to
changing requirements with minimal risk.

### IV. Test-Driven Development (TDD)

TDD is NON-NEGOTIABLE for all feature implementation:

- **Red-Green-Refactor Cycle**:
  1. Write a failing test that defines expected behavior (RED)
  2. Write minimal code to make the test pass (GREEN)
  3. Refactor while keeping tests green (REFACTOR)
- **Test First**: No production code MUST be written without a failing test first.
- **One Assert Per Test**: Each test MUST verify a single behavior.
- **Fast Tests**: Unit tests MUST execute in under 100ms each.
- **Independent Tests**: Tests MUST NOT depend on execution order or shared state.

**Rationale**: Ensures comprehensive test coverage, drives better design, and provides
living documentation of system behavior.

### V. Behavior-Driven Development (BDD)

User-facing features MUST be specified using BDD:

- **Gherkin Syntax**: Acceptance criteria MUST use Given/When/Then format.
- **Executable Specifications**: BDD scenarios MUST be implemented as automated tests.
- **User Story Mapping**: Each scenario MUST trace to a user story and business value.
- **Scenario Structure**:
  - **Given**: Initial context/preconditions
  - **When**: Action or event triggering the behavior
  - **Then**: Expected outcome (observable and verifiable)

**Rationale**: Bridges communication gap between business and technical teams, ensures
features deliver actual user value.

### VI. Code Quality Standards

All code MUST meet these quality gates:

- **Static Analysis**: Code MUST pass linting with zero warnings. Type checking MUST
  be enabled and pass without errors.
- **Test Coverage**: Minimum 80% line coverage for domain/application layers.
  Critical paths MUST have 100% branch coverage.
- **Cyclomatic Complexity**: Methods MUST NOT exceed complexity of 10. Functions
  exceeding this MUST be refactored.
- **Documentation**: Public APIs MUST have documentation. Complex algorithms MUST
  include inline comments explaining intent.
- **Code Review**: All changes MUST be reviewed before merge. Reviewers MUST verify
  principle compliance.

**Rationale**: Maintains consistent quality, reduces technical debt, and ensures
long-term maintainability.

## Architectural Constraints

### Layer Dependencies

```
┌─────────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                          │
│  (Frameworks, DBs, External APIs, UI, Controllers, Adapters)    │
├─────────────────────────────────────────────────────────────────┤
│                    Application Layer                             │
│  (Use Cases, Application Services, Ports/Interfaces)            │
├─────────────────────────────────────────────────────────────────┤
│                    Domain Layer                                  │
│  (Entities, Value Objects, Domain Services, Domain Events)      │
└─────────────────────────────────────────────────────────────────┘
        ↑ Dependencies point INWARD only (toward Domain)
```

- Infrastructure → Application → Domain (allowed)
- Domain → Application → Infrastructure (FORBIDDEN)
- Horizontal dependencies within same layer MUST be minimized

### Framework Isolation

- All framework code MUST reside in the infrastructure layer
- Framework annotations/decorators on domain objects are FORBIDDEN
- Framework upgrades MUST NOT require domain layer changes
- Domain tests MUST run without framework initialization

### Port/Adapter Implementation

- **Ports**: Interfaces defined in application layer specifying required capabilities
- **Adapters**: Infrastructure implementations of ports
- **Dependency Injection**: Ports MUST be injected into use cases at composition root
- **Adapter Selection**: Runtime adapter selection MUST be configurable without code changes

## Testing Standards

### Test Pyramid

```
        /\
       /  \      E2E Tests (Few - critical user journeys only)
      /────\
     /      \    Integration Tests (Moderate - adapter/port verification)
    /────────\
   /          \  Unit Tests (Many - domain logic, use cases)
  /────────────\
```

### Test Categories

| Category | Scope | Speed | Isolation |
|----------|-------|-------|-----------|
| Unit | Single class/function | < 100ms | Full (mocked dependencies) |
| Integration | Adapter + real dependency | < 5s | Partial (real DB/API) |
| Contract | Port/Adapter interface compliance | < 1s | Mocked external systems |
| E2E | Full user journey | < 30s | None (real environment) |

### Test Naming Convention

- `test_<unit>_<scenario>_<expected_outcome>`
- Example: `test_order_service_when_items_empty_raises_validation_error`

### Test Organization

```
tests/
├── unit/           # Domain and application layer tests
├── integration/    # Adapter tests with real dependencies
├── contract/       # Port interface compliance tests
└── e2e/            # End-to-end user journey tests
```

## Governance

### Amendment Process

1. Propose change via documented rationale
2. Review impact on existing codebase
3. Update dependent templates and documentation
4. Version bump following semantic versioning
5. Communicate changes to all team members

### Versioning Policy

- **MAJOR**: Principle removal, redefinition, or backward-incompatible governance change
- **MINOR**: New principle added, section expanded, or significant guidance added
- **PATCH**: Clarification, typo fix, or non-semantic refinement

### Compliance Review

- All PRs MUST verify compliance with this constitution
- Code review checklist MUST include principle verification
- Architectural Decision Records (ADRs) MUST reference relevant principles
- Quarterly audits MUST assess overall compliance and identify technical debt

### Exceptions

- Exceptions to principles MUST be documented with justification
- Exceptions require approval from technical lead
- Exceptions MUST have planned remediation timeline
- Complexity tracking table MUST record all exceptions

**Version**: 1.0.0 | **Ratified**: 2026-01-09 | **Last Amended**: 2026-01-09
