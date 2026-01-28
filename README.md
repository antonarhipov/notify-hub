# NotifyHub Demo Application

A Spring Boot notification service designed to showcase IntelliJ IDEA Ultimate features across three key scenarios.

## Overview

NotifyHub demonstrates:
1. **Bean Navigation** - Multiple `NotificationSender` implementations with `@Primary`/`@Qualifier`
2. **Debugger Insights** - `@Value` property resolution across config profiles (dev/prod)
3. **Database Tooling** - JPQL/SQL autocomplete, schema awareness, and navigation

## Technologies

- Spring Boot 4.0.2
- Spring Data JPA with Hibernate
- H2 (dev) and PostgreSQL (prod) databases
- Lombok for code generation
- Bean Validation
- Testcontainers for integration testing

## Project Structure

```
com.jetbrains.notifyhub/
├── model/              # Domain entities and DTOs
│   ├── Notification.java          # Request DTO (record)
│   ├── NotificationResult.java    # Response DTO (record)
│   ├── NotificationTemplate.java  # JPA entity (no Lombok)
│   └── NotificationLog.java       # JPA entity (no Lombok)
├── repository/         # Spring Data JPA repositories
│   ├── NotificationTemplateRepository.java  # JPQL & native SQL queries
│   └── NotificationLogRepository.java
├── service/            # Business logic layer
│   ├── NotificationSender.java              # Interface
│   ├── EmailNotificationSender.java         # Implementation
│   ├── SmsNotificationSender.java           # Implementation
│   ├── PushNotificationSender.java          # @Primary implementation
│   ├── TemplateResolver.java                # Template resolution
│   └── NotificationDispatcher.java          # PRIMARY DEMO FILE
├── controller/         # REST API
│   └── NotificationController.java
├── config/             # Spring configuration
│   ├── MessageSourceConfig.java
│   └── NotificationConfig.java    # Bean ambiguity demo
└── audit/              # Audit components
    └── NotificationAuditor.java
```

## Building and Running

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker (optional, for PostgreSQL and Testcontainers)

### Build

```bash
./mvnw clean package
```

### Run with H2 (Development)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

H2 Console available at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:notifyhub`
- Username: `sa`
- Password: (empty)

### Run with PostgreSQL (Production)

1. Start PostgreSQL:
```bash
docker run -e POSTGRES_PASSWORD=secret -e POSTGRES_USER=notifyhub_app \
  -e POSTGRES_DB=notifyhub -p 5432:5432 postgres:16
```

2. Run application:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod -DDB_PASSWORD=secret
```

### Run Tests

```bash
./mvnw test
```

Note: Tests use H2 in-memory database. To use Testcontainers with PostgreSQL, uncomment the code in `IntegrationTestBase.java` (requires Docker).

## API Endpoints

### Send Notification

```bash
POST /api/notify
Content-Type: application/json

{
  "recipient": "user@example.com",
  "channel": "email",           // optional: email, sms, push
  "templateCode": "welcome",
  "locale": "en",
  "payload": {}
}
```

Example:
```bash
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"user@example.com","channel":"email","templateCode":"welcome","locale":"en","payload":{}}'
```

Response:
```json
{
  "success": true,
  "message": "Notification sent successfully",
  "notificationId": "9c3fff2d-ea80-4fce-899a-cc6da7e708a7"
}
```

### Health Check

```bash
GET /api/health
```

## IntelliJ IDEA Demo Scenarios

> **Note**: All demo points are marked with `//DEMO` comments in the code.
> See `DEMO_MARKERS.md` for a complete list and `QUICK_DEMO_GUIDE.md` for a 3-minute demo script.

### Scenario 1: Bean Navigation & Injection

**File**: `NotificationDispatcher.java`

1. **Multiple Implementation Navigation**
   - Open `NotificationDispatcher.java:52`
   - Click gutter icon on `List<NotificationSender> senders` field
   - See all 3 implementations: `EmailNotificationSender`, `SmsNotificationSender`, `PushNotificationSender`
   - Navigate to any implementation

2. **Lombok Constructor Injection**
   - Open `TemplateResolver.java`
   - Note: `@RequiredArgsConstructor` annotation but no visible constructor
   - Gutter icons on fields show dependency injection
   - IDE understands Lombok-generated code

3. **Bean Ambiguity Warning**
   - Open `NotificationConfig.java:48`
   - Yellow warning on `NotificationSender sender` parameter
   - Gutter icon shows 3 candidate beans
   - `@Primary` annotation on `PushNotificationSender` makes it the default
   - Fix by adding: `@Qualifier("emailNotificationSender")`

### Scenario 2: Debugger with Spring Insights

**File**: `NotificationDispatcher.java`

1. **Setup**
   - Set breakpoint in `NotificationDispatcher.dispatch()` at line 68
   - Run with prod profile: `--spring.profiles.active=prod`
   - Send POST request to `/api/notify`

2. **Observe @Value Resolution**
   - In debugger Variables pane, inspect:
     - `defaultChannel` = "push" (from `application-prod.yml`, not "email" from base)
     - `rateLimit` = 200 (from `application-prod.yml`, not 50 from base)
     - `maxRetryAttempts` = 3 (from `application.yml`, no override)
   - Evaluate expression: `this.defaultChannel` returns "push"
   - Shows resolved values from active profile configuration

3. **Profile Switching**
   - Run with dev profile: observe `defaultChannel` = "email", `rateLimit` = 50
   - Environment variable override: `NOTIFYHUB_RATE_LIMIT=100` overrides YAML

### Scenario 3: Database Tooling

**File**: `NotificationTemplateRepository.java`

1. **JPQL Autocomplete** (line 28)
   - Type: `SELECT t FROM Notif` → autocomplete suggests `NotificationTemplate`
   - Type: `WHERE t.ch` → autocomplete suggests `channel`
   - All entity fields available: `channel`, `templateCode`, `active`, `updatedAt`, etc.
   - Ctrl+Click on entity name navigates to `NotificationTemplate.java`

2. **Native SQL Autocomplete** (line 38)
   - Type: `FROM notif` → autocomplete suggests `notification_template`
   - Type: `WHERE temp` → autocomplete suggests `template_code`
   - Column names validated against schema
   - Typo detection: `WHERE template_cod` → red squiggle with error

3. **Schema Navigation**
   - Ctrl+Click on `notification_template` in query
   - Navigates to `schema.sql` DDL definition
   - Or opens database tool window with table structure

4. **Database Tool Window**
   - Open Database tool window
   - Connect to H2 datasource using config from `application.yml`
   - Expand schema tree: see `notification_template` and `notification_log` tables
   - View columns, types, constraints, and indices
   - Right-click table → Jump to DDL

5. **Execute Query from Editor**
   - Click gutter icon on `@Query` annotation
   - Executes query directly from repository interface
   - Results appear in database tool window

## Configuration Profiles

### Base Configuration (`application.yml`)
- Default channel: `email`
- Rate limit: `50`
- Max retry attempts: `3`
- H2 in-memory database
- SQL initialization enabled

### Dev Profile (`application-dev.yml`)
- H2 console enabled at `/h2-console`
- SQL logging enabled
- Debug logging for application

### Prod Profile (`application-prod.yml`)
- PostgreSQL database
- Default channel: `push` (override)
- Rate limit: `200` (override)
- Production-optimized settings

## Database Schema

### notification_template
Stores notification templates with locale support.

| Column             | Type         | Description                    |
|--------------------|--------------|--------------------------------|
| id                 | BIGINT       | Primary key                    |
| channel            | VARCHAR(50)  | Notification channel           |
| template_code      | VARCHAR(100) | Template identifier            |
| subject_template   | VARCHAR(500) | Email subject template         |
| body_template      | TEXT         | Message body template          |
| locale             | VARCHAR(10)  | Language/locale code           |
| active             | BOOLEAN      | Template active status         |
| created_at         | TIMESTAMP    | Creation timestamp             |
| updated_at         | TIMESTAMP    | Last update timestamp          |

**Unique Constraint**: (channel, template_code, locale)

### notification_log
Audit log for sent notifications.

| Column             | Type         | Description                    |
|--------------------|--------------|--------------------------------|
| id                 | BIGINT       | Primary key                    |
| notification_id    | VARCHAR(100) | Unique notification ID         |
| recipient          | VARCHAR(255) | Recipient address              |
| channel            | VARCHAR(50)  | Used channel                   |
| template_code      | VARCHAR(100) | Template code                  |
| status             | VARCHAR(20)  | SUCCESS or FAILED              |
| sent_at            | TIMESTAMP    | Send timestamp                 |
| error_message      | TEXT         | Error details if failed        |
| created_at         | TIMESTAMP    | Log entry creation             |

**Indices**: recipient, (status, sent_at), notification_id

## Sample Templates

The application seeds sample templates in `data.sql`:

- **Email**: welcome (en, es), password-reset (en)
- **SMS**: welcome (en), verification-code (en)
- **Push**: welcome (en), new-message (en)

## Testing

### Unit Tests
Run with: `./mvnw test`

Tests use H2 in-memory database by default.

### Integration Tests with Testcontainers
To enable Testcontainers (requires Docker):

1. Edit `src/test/java/com/jetbrains/notifyhub/IntegrationTestBase.java`
2. Uncomment the `@Testcontainers` annotation and PostgreSQL container field
3. Run tests: `./mvnw test`

Testcontainers will automatically start a PostgreSQL container for testing.

## Key Features for Demo

1. **Bean Management**
   - Multiple implementations of `NotificationSender` interface
   - `@Primary` annotation on `PushNotificationSender`
   - `List<NotificationSender>` injection shows all implementations
   - Bean ambiguity demonstration in `NotificationConfig`

2. **Configuration Management**
   - Profile-specific property resolution
   - `@Value` field debugging shows resolved values
   - Environment variable override support
   - Clear profile inheritance demonstration

3. **Database Tooling**
   - JPQL queries with entity autocomplete
   - Native SQL queries with table/column autocomplete
   - Schema validation and error detection
   - Direct query execution from code
   - Database tool window integration

4. **Code Quality**
   - Lombok integration for reduced boilerplate
   - Bean Validation with `@Valid`
   - Explicit code in JPA entities (no Lombok)
   - Spring Data JPA method queries

## Troubleshooting

### Tests fail with Docker errors
Tests use H2 by default. If you see Testcontainers errors, either:
- Start Docker, or
- Keep Testcontainers code commented in `IntegrationTestBase.java`

### Application won't start
- Check Java version: `java -version` (requires Java 21+)
- Check port 8080 is available: `lsof -i :8080`
- For prod profile, ensure PostgreSQL is running

### H2 Console not accessible
- Confirm dev profile is active
- Check URL: http://localhost:8080/h2-console
- Use JDBC URL: `jdbc:h2:mem:notifyhub`, username: `sa`, password: (empty)

## License

This is a demo application for educational purposes.
