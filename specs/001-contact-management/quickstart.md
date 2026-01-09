# Quickstart Guide: Contact Management System

**Feature Branch**: `001-contact-management`
**Date**: 2026-01-10

## Prerequisites

- **Java 17** or later
- **Gradle 8.x** (or use the included wrapper)
- **IDE**: IntelliJ IDEA recommended (with Lombok plugin if using)
- **Git**: For version control

## Project Setup

### 1. Clone and checkout the branch

```bash
git clone <repository-url>
cd aop-poc
git checkout 001-contact-management
```

### 2. Build the project

```bash
./gradlew build
```

This will:
- Download dependencies
- Compile source code
- Run unit tests
- Run Cucumber BDD tests
- Generate JaCoCo coverage report

### 3. Run the application

**Development mode (H2 database)**:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The application will start at `http://localhost:8080`.

**Production mode (PostgreSQL)**:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=contactdb
export DB_USER=postgres
export DB_PASSWORD=yourpassword

./gradlew bootRun --args='--spring.profiles.active=prod'
```

## Quick Test

### Access H2 Console (Development only)

1. Navigate to `http://localhost:8080/h2-console`
2. JDBC URL: `jdbc:h2:mem:contactdb`
3. Username: `sa`
4. Password: (leave empty)

### Create a Contact

```bash
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "張三",
    "phone": "0912345678",
    "address": "台北市中正區"
  }'
```

Expected response (HTTP 201):
```json
{
  "id": 1,
  "name": "張三",
  "phone": "0912345678",
  "address": "台北市中正區",
  "createdAt": "2026-01-10T10:30:00",
  "updatedAt": "2026-01-10T10:30:00"
}
```

### Get All Contacts

```bash
curl http://localhost:8080/api/contacts
```

### Get a Contact by ID

```bash
curl http://localhost:8080/api/contacts/1
```

### Update a Contact

```bash
curl -X PUT http://localhost:8080/api/contacts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "張三豐",
    "phone": "0912345678",
    "address": "台北市信義區"
  }'
```

### Delete a Contact

```bash
curl -X DELETE http://localhost:8080/api/contacts/1
```

### Query Audit Logs

```bash
# Get all audit logs
curl "http://localhost:8080/api/audit-logs"

# Filter by operation type
curl "http://localhost:8080/api/audit-logs?operationType=CREATE"

# Filter by time range
curl "http://localhost:8080/api/audit-logs?startTime=2026-01-10T00:00:00&endTime=2026-01-10T23:59:59"

# Filter by client IP
curl "http://localhost:8080/api/audit-logs?clientIp=127.0.0.1"

# Combine filters with pagination
curl "http://localhost:8080/api/audit-logs?operationType=CREATE&page=0&size=10"
```

## Running Tests

### Run all tests
```bash
./gradlew test
```

### Run only unit tests
```bash
./gradlew test --tests "*Unit*"
```

### Run only integration tests
```bash
./gradlew test --tests "*Integration*"
```

### Run Cucumber BDD tests
```bash
./gradlew test --tests "*CucumberTest*"
```

### Generate coverage report
```bash
./gradlew jacocoTestReport
```
Report available at: `build/reports/jacoco/test/html/index.html`

### Verify coverage threshold (80%)
```bash
./gradlew jacocoTestCoverageVerification
```

## Project Structure Overview

```
src/
├── main/java/com/example/contact/
│   ├── domain/              # Domain Layer (pure Java)
│   │   ├── model/           # Entities, Value Objects
│   │   └── event/           # Domain events (if any)
│   │
│   ├── application/         # Application Layer
│   │   ├── port/in/         # Input ports (use cases)
│   │   ├── port/out/        # Output ports (repositories)
│   │   └── service/         # Application services
│   │
│   └── infrastructure/      # Infrastructure Layer
│       ├── adapter/in/web/  # REST controllers
│       ├── adapter/out/     # JPA adapters
│       ├── aop/             # AOP aspects
│       └── config/          # Spring configuration
│
└── test/
    ├── java/.../
    │   ├── unit/            # Unit tests
    │   ├── integration/     # Integration tests
    │   └── contract/        # Contract tests
    └── resources/features/  # Cucumber feature files
```

## Key Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Gradle build configuration |
| `src/main/resources/application.yml` | Common configuration |
| `src/main/resources/application-dev.yml` | H2 database (development) |
| `src/main/resources/application-prod.yml` | PostgreSQL (production) |

## Common Issues

### Issue: Build fails with "Could not resolve dependencies"
**Solution**: Ensure you have internet access and check your Gradle proxy settings.

### Issue: H2 console shows "Database not found"
**Solution**: Make sure the application is running with `-dev` profile.

### Issue: Tests fail with "Connection refused"
**Solution**: For integration tests, ensure no other application is using port 8080.

### Issue: Coverage verification fails
**Solution**: Add more tests to domain/application layers to meet the 80% threshold.

## Next Steps

1. Review the [spec.md](./spec.md) for detailed requirements
2. Check [data-model.md](./data-model.md) for entity definitions
3. See [contracts/openapi.yaml](./contracts/openapi.yaml) for complete API documentation
4. Read [research.md](./research.md) for technical decisions

## API Documentation

When the application is running, API documentation is available at:
- OpenAPI YAML: See `specs/001-contact-management/contracts/openapi.yaml`
- Swagger UI: (Optional - add `springdoc-openapi` dependency for live docs)
