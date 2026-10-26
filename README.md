# NotifyHub

NotifyHub is a small notification service built with Spring Boot, Java and Kotlin. It routes notifications to
pluggable channels (email, SMS, push and any channel you add at runtime), records every outcome in a PostgreSQL
audit log, and ships a "Mission Control" dashboard to watch statistics and switch channel services on and off.

It also doubles as a demo project for IntelliJ IDEA Ultimate features (bean navigation, the debugger with Spring
`@Value` resolution, database tooling, mixed Java/Kotlin code). See [IntelliJ IDEA demo scenarios](#intellij-idea-demo-scenarios).

## Features

- **Notification API** - `POST /api/notify` with validation, template/locale support and optional content-enrichment
  `rules` (`transform.header`, `transform.footer`, `transform.style`). Protected with HTTP Basic (`ADMIN` role).
- **Mission Control dashboard** - static UI at `/` backed by `/api/dashboard/**`: totals, success rate, per-channel
  breakdown, recent activity, per-channel enable/disable toggles, "add channel" and a test-notification form.
- **Channel registry** - `ChannelService` discovers every `NotificationSender` bean and lets you register new
  channel types at runtime. Notifications to a disabled channel are recorded as `BLOCKED`.
- **Audit log** - every outcome (`SENT`, `FAILED`, `BLOCKED`) is stored in `notification_log`.
- **Flyway migrations** - schema and seed templates are managed in `src/main/resources/db/migration`.
- **Mixed Java/Kotlin build** - Kotlin sources are compiled first and are used from the Java code (and vice versa).
- **Docker Compose stack** - PostgreSQL, the API and an nginx reverse proxy.

## Technologies

- Spring Boot 4.0.2 (Web MVC, Data JPA/Hibernate, Validation, Security, Actuator)
- PostgreSQL 16 and Flyway
- Java 21 (build targets release 21) and Kotlin 2.2 (version managed by the Spring Boot parent)
- Lombok (used selectively in services and controllers)
- JUnit 5, Mockito, Spring Security Test and Testcontainers (PostgreSQL)
- Docker, Docker Compose and nginx

## Project Structure

```
.
├── pom.xml                      # Maven build (Kotlin compiled before javac)
├── mvnw, mvnw.cmd, .mvn/        # Maven wrapper (Maven 3.9.12)
├── Dockerfile                   # Multi-stage build: JDK 21 build -> JRE 21 runtime
├── docker-compose.yaml          # postgres, api, web (nginx) and a deliberately failing "broken" service
├── nginx/nginx.conf             # Reverse proxy: port 8080 -> api:8080
├── demo-test.sh                 # curl smoke script (predates Basic auth: add `-u admin:admin` to its curl calls)
└── src
    ├── main
    │   ├── java/com/example/notifyhub
    │   │   ├── NotifyHubApplication.java
    │   │   ├── api/             # REST controllers
    │   │   │   ├── NotificationController.java   # POST /api/notify, GET /api/health
    │   │   │   └── DashboardController.java      # /api/dashboard/**
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java           # HTTP Basic, ADMIN-only notification API
    │   │   ├── model/           # Notification (record), NotificationResult, JPA entities, dashboard DTOs
    │   │   ├── repository/      # NotificationTemplateRepository, NotificationLogRepository
    │   │   └── service/
    │   │       ├── NotificationDispatcher.java   # Core routing, rules pipeline and audit logging
    │   │       ├── ChannelService.java           # Channel registry and on/off state
    │   │       ├── DashboardService.java         # Statistics for the dashboard
    │   │       ├── TemplateResolver.java         # Template lookup with locale fallback
    │   │       ├── NotificationSender.java       # Sender interface
    │   │       ├── Email/Sms/PushNotificationSender.java
    │   │       ├── GenericNotificationSender.java  # Backs channels registered at runtime
    │   │       └── transformers/                 # Header, Footer and Style message transformers
    │   ├── kotlin/com/example/notifyhub/text
    │   │   └── MessageDecorations.kt             # maskEmail(), footerLine(), routingLabel
    │   └── resources
    │       ├── application.yml, application-dev.yml, application-prod.yml
    │       ├── messages.properties
    │       ├── db/migration/    # V1 schema, V2 seed templates
    │       └── static/          # Dashboard (index.html, css/, js/)
    └── test                     # Unit, MockMvc and Testcontainers integration tests
```

## Building and Running

### Prerequisites

- JDK 21 or newer (the build targets release 21; verified with JDK 27)
- Docker (PostgreSQL for local runs, the Compose stack and the integration tests)

Maven does not need to be installed; use the wrapper (`./mvnw`).

### Build

```bash
./mvnw clean package            # build and run tests (tests need Docker)
./mvnw -DskipTests package      # build the jar without running tests
```

The executable jar is created at `target/notify-hub-0.0.1-SNAPSHOT.jar`.

> **JDK 27 and Lombok**: the Spring Boot parent manages Lombok 1.18.42, which crashes `javac` 27 with
> `ExceptionInInitializerError: com.sun.tools.javac.tree.EndPosTable`. `pom.xml` therefore overrides
> `<lombok.version>` with 1.18.48. Keep that override if you build with JDK 27.

### Run locally

The default configuration expects PostgreSQL on `localhost:5432` (database `notifyhub`, user `notifyhub_app`,
password `secret`). Start only the database from the Compose file, then run the app:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

Flyway creates the schema and seeds the sample templates on startup.

Open the dashboard at http://localhost:8080 (the standalone app listens on port 8080).

Use the `dev` profile for formatted SQL logging:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run the whole stack with Docker Compose

```bash
docker compose up --build
```

| Service    | Description                                                              | Port             |
|------------|--------------------------------------------------------------------------|------------------|
| `postgres` | PostgreSQL 16 with a persistent volume                                   | 5432             |
| `api`      | NotifyHub built from the `Dockerfile`, running with the `prod` profile   | internal (8080)  |
| `web`      | nginx reverse proxy in front of `api`                                    | http://localhost:8080 |
| `broken`   | Container that logs `[error]` lines and exits with code 1 on purpose, for demonstrating failing services | - |

Stop and remove everything (including the database volume) with `docker compose down -v`.

## Security

`SecurityConfig` protects the notification API with HTTP Basic authentication; everything else (dashboard UI,
`/api/dashboard/**`, static assets, actuator) is open.

| Endpoint                          | Access          |
|-----------------------------------|-----------------|
| `POST /api/notify`, `GET /api/health` | `ADMIN` role (HTTP Basic) |
| Everything else                   | Public          |

The single in-memory administrator defaults to `admin` / `admin`. Override it with environment variables
(or `notifyhub.security.admin.*` properties):

```bash
ADMIN_USERNAME=ops ADMIN_PASSWORD='change-me' ./mvnw spring-boot:run
```

> The defaults are for local development only. Always set `ADMIN_PASSWORD` outside local use.
> CSRF protection is disabled because the API is stateless and uses per-request Basic credentials.
> The dashboard's test-notification form calls `/api/notify`, so your browser asks for the admin credentials.

## API

### Send a notification

`POST /api/notify` (requires admin credentials)

| Field          | Required | Description                                                          |
|----------------|----------|----------------------------------------------------------------------|
| `recipient`    | yes      | Address of the recipient (not blank)                                 |
| `templateCode` | yes      | Template identifier, for example `welcome`                           |
| `channel`      | no       | `email`, `sms`, `push` or any registered channel; falls back to `notifyhub.default-channel` (`email`) |
| `locale`       | no       | Defaults to `en`                                                     |
| `payload`      | no       | Free-form template data                                              |
| `rules`        | no       | Map of enrichment rules, see below                                   |

```bash
curl -u admin:admin -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{
        "recipient": "user@example.com",
        "channel": "email",
        "templateCode": "welcome",
        "locale": "en",
        "payload": {},
        "rules": {
          "transform.header": "Important Update",
          "transform.footer": "Sent via NotifyHub",
          "transform.style": "formal"
        }
      }'
```

Successful response (`200`):

```json
{
  "success": true,
  "message": "Notification sent successfully",
  "notificationId": "9c3fff2d-ea80-4fce-899a-cc6da7e708a7"
}
```

Other outcomes:

| Status | Reason                                                                                   |
|--------|------------------------------------------------------------------------------------------|
| `400`  | Validation error, unknown channel, a disabled channel service, or a sender failure (`success: false` with a message) |
| `401`  | Missing or invalid credentials                                                           |
| `403`  | Authenticated, but without the `ADMIN` role                                              |

**Rules.** Rules whose key starts with `transform.` are applied by the matching `MessageTransformer`
(`header`, `footer`, `style`). A rule whose value is `disabled` is meant to be skipped (see the known issue under
[Testing](#testing)). Rules never influence which channel is used.

### Health check

```bash
curl -u admin:admin http://localhost:8080/api/health
```

### Dashboard API

These endpoints are open and back the Mission Control UI.

| Method and path                                   | Description                                                    |
|---------------------------------------------------|----------------------------------------------------------------|
| `GET /api/dashboard/stats`                        | Totals, success rate, per-channel stats and the 10 latest notifications |
| `GET /api/dashboard/services`                     | On/off state of every channel service                          |
| `POST /api/dashboard/services`                    | Register a new channel, body `{"channel": "webhook"}` (`201`, `409` if it exists, `400` if blank) |
| `POST /api/dashboard/services/{channel}/enable`   | Enable a channel (`404` for an unknown channel)                |
| `POST /api/dashboard/services/{channel}/disable`  | Disable a channel                                              |
| `PUT /api/dashboard/services/{channel}`           | Set the state explicitly, body `{"enabled": true}`             |

Channel state is held in memory; all channels start enabled after every restart, and channels registered at runtime
are not persisted.

## Configuration

Configuration lives in `src/main/resources`.

| Property                              | Default / source                | Description                                    |
|---------------------------------------|---------------------------------|------------------------------------------------|
| `spring.datasource.url`               | `jdbc:postgresql://localhost:5432/notifyhub` | Database URL (`SPRING_DATASOURCE_URL` overrides it) |
| `spring.datasource.username`          | `notifyhub_app`                 | Database user                                  |
| `spring.datasource.password`          | `${DB_PASSWORD:secret}`         | Database password                              |
| `notifyhub.default-channel`           | `email` (`push` in `prod`)      | Channel used when a request has none           |
| `notifyhub.rate-limit`                | `50` (`200` in `prod`)          | Rate limit value                               |
| `notifyhub.retry.max-attempts`        | `3`                             | Retry attempts                                 |
| `notifyhub.security.admin.username`   | `${ADMIN_USERNAME:admin}`       | Admin user for the notification API            |
| `notifyhub.security.admin.password`   | `${ADMIN_PASSWORD:admin}`       | Admin password for the notification API        |

Any property can be overridden through environment variables, for example `NOTIFYHUB_RATE_LIMIT=100`.

### Profiles

- **default** (`application.yml`): PostgreSQL on localhost, Flyway enabled, `ddl-auto: none`, SQL logging on.
- **`dev`** (`application-dev.yml`): formatted SQL and DEBUG logging for Hibernate SQL.
- **`prod`** (`application-prod.yml`): PostgreSQL dialect, default channel `push`, rate limit `200`. Used by the Compose `api` service.
- **`test`** (`src/test/resources/application-test.yml`): used by the integration tests.

## Database Schema

Managed by Flyway (`db/migration`). Hibernate does not alter the schema (`ddl-auto: none`).

### notification_template

Notification templates with locale support.

| Column           | Type         | Description                |
|------------------|--------------|----------------------------|
| id               | BIGSERIAL    | Primary key                |
| channel          | VARCHAR(50)  | Notification channel       |
| template_code    | VARCHAR(100) | Template identifier        |
| subject_template | VARCHAR(500) | Subject template (email)   |
| body_template    | TEXT         | Message body template      |
| locale           | VARCHAR(10)  | Language/locale code (`en` by default) |
| active           | BOOLEAN      | Whether the template is active |
| created_at       | TIMESTAMP    | Creation timestamp         |
| updated_at       | TIMESTAMP    | Last update timestamp      |

**Unique constraint**: `(channel, template_code, locale)`

### notification_log

Audit log of every dispatch attempt.

| Column          | Type         | Description                                    |
|-----------------|--------------|------------------------------------------------|
| id              | BIGSERIAL    | Primary key                                    |
| notification_id | VARCHAR(100) | Generated notification ID                      |
| recipient       | VARCHAR(255) | Recipient address                              |
| channel         | VARCHAR(50)  | Channel used                                   |
| template_code   | VARCHAR(100) | Template code                                  |
| status          | VARCHAR(20)  | `SENT`, `FAILED` or `BLOCKED`                  |
| sent_at         | TIMESTAMP    | Send timestamp                                 |
| error_message   | TEXT         | Error details when not sent                    |
| created_at      | TIMESTAMP    | Log entry creation                             |

**Indices**: `recipient`, `(status, sent_at)`, `notification_id`

### Seed templates

`V2__seed_notification_templates.sql` inserts:

- **Email**: `welcome` (en, es), `password-reset` (en)
- **SMS**: `welcome` (en), `verification-code` (en)
- **Push**: `welcome` (en), `new-message` (en)

## Testing

```bash
./mvnw test
```

- **Integration tests** (`NotifyHubApplicationTests`, `NotificationControllerSecurityTest`,
  `DashboardControllerTest`, `DashboardIntegrationTest`, `NotificationTemplateRepositoryTest`) extend
  `IntegrationTestBase`, which starts a `postgres:16-alpine` Testcontainer once per JVM and wires it in with
  `@ServiceConnection`. Flyway migrations run against real PostgreSQL. **Docker must be running.**
- **Unit tests** (`NotificationDispatcherTest`) use Mockito only and need neither a database nor Docker.

Run a single test class:

```bash
./mvnw test -Dtest=NotificationDispatcherTest
```

> **Known issue**: `NotificationDispatcherTest.skipsDisabledRules` currently fails. `NotificationDispatcher` compares
> the rule value with `!=` (reference comparison) instead of `equals`, so rules whose value is `disabled` are still
> applied. This is a deliberately planted bug used in the debugging demo (see the git history); the other tests
> are expected to pass.

## IntelliJ IDEA demo scenarios

Demo points are marked with `//DEMO` comments in the code.

### Scenario 1: Bean navigation and injection

- Open `NotificationSender.java` and click the gutter icon to jump to `EmailNotificationSender`,
  `SmsNotificationSender` and `PushNotificationSender`.
- `ChannelService` and `NotificationDispatcher` receive these beans as `List<NotificationSender>` /
  `List<MessageTransformer>`; use the gutter icons on the constructor parameters to see every candidate bean.
- `PushNotificationSender` has `@Primary` commented out - uncomment it to see how the IDE marks the primary candidate.
- `TemplateResolver` uses Lombok's `@RequiredArgsConstructor`: there is no visible constructor, but the IDE
  understands the generated code and shows dependency-injection gutter icons on the fields.

### Scenario 2: Debugger with Spring `@Value` resolution

**File**: `NotificationDispatcher.java`

1. Set a breakpoint in `dispatch()`.
2. Run with the `prod` profile (`--spring.profiles.active=prod`) and send a request to `/api/notify`.
3. In the Variables pane, inspect the resolved values:
   - `defaultChannel` = `push` (from `application-prod.yml`; the fallback is `email`)
   - `rateLimit` = `200` (from `application-prod.yml`; base value is `50`)
   - `maxRetryAttempts` = `3` (from `application.yml`)
4. Switch profiles or set `NOTIFYHUB_RATE_LIMIT=100` to see the value change.
5. Step through the rules pipeline (`filter` -> `filter` -> `map`) with the Stream Trace.

### Scenario 3: Database tooling

**File**: `NotificationTemplateRepository.java`

1. **JPQL autocomplete** - in `findActiveByChannel`, type `SELECT t FROM Notif` and `WHERE t.ch` to get entity and
   field suggestions.
2. **Native SQL autocomplete** - in `findByCodeLocaleAndChannel`, type `FROM notif` and `WHERE temp`. A typo such as
   `template_cod` is highlighted.
3. **Schema navigation** - Ctrl/Cmd+Click on `notification_template` to jump to
   `db/migration/V1__create_notification_schema.sql`.
4. **Database tool window** - connect to the PostgreSQL started by Docker Compose (`localhost:5432`, database
   `notifyhub`, user `notifyhub_app`, password `secret`) and browse `notification_template` and `notification_log`.
5. **Run queries from the editor** - use the gutter icon on a `@Query` annotation.

### Scenario 4: Mixed Java and Kotlin

`MessageDecorations.kt` is called from Java (`NotificationDispatcher`, `EmailNotificationSender`,
`FooterTransformer`). Navigate between the two languages, and note that Kotlin's `Notification.routingLabel`
property appears in Java as `MessageDecorations.getRoutingLabel(...)`. Kotlin is compiled first by
`kotlin-maven-plugin`, then `javac` runs with Lombok on the combined sources.

## Troubleshooting

### `ExceptionInInitializerError: com.sun.tools.javac.tree.EndPosTable`
Lombok is too old for your JDK. The Lombok version managed by Spring Boot 4.0.2 (1.18.42) fails on JDK 27, so keep the
`<lombok.version>1.18.48</lombok.version>` override in `pom.xml`.

### Tests fail with Docker or Testcontainers errors
The integration tests start a PostgreSQL container, so Docker must be running. Use `./mvnw -DskipTests package`
to build without tests.

### Application fails to start with a connection error
- PostgreSQL must be reachable on `localhost:5432`: `docker compose up -d postgres`.
- Check that port 5432 and port 8080 are free: `lsof -i :5432`, `lsof -i :8080`.
- A different password needs `DB_PASSWORD=<password>`.

### `401 Unauthorized` on `/api/notify` or `/api/health`
These endpoints need HTTP Basic credentials (default `admin` / `admin`), for example `curl -u admin:admin ...`.

### The `broken` Compose service exits immediately
That is intentional; it exists to demonstrate failing services. Run `docker compose up --build postgres api web` to skip it.

## License

This is a demo application for educational purposes.
