# NotifyHub Demo Application

> **Purpose:** Demonstrate IntelliJ IDEA's value for Spring developers through a realistic notification service application.
>
> **Target:** Product landing page screenshots and screencasts showing Spring Support → Debugger → Database tooling.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Application Overview](#application-overview)
3. [Scenario 1: Spring Support](#scenario-1-spring-support-bean-navigation--injection)
4. [Scenario 2: Debugger with Spring Insights](#scenario-2-debugger-with-spring-insights)
5. [Scenario 3: Database Tooling](#scenario-3-database--datasource-support)
6. [Technical Specifications](#technical-specifications)
7. [File Structure](#file-structure)
8. [Screenshot Capture Guide](#screenshot-capture-guide)

---

## Executive Summary

**NotifyHub** is a multi-channel notification dispatch service designed specifically to showcase IntelliJ IDEA's Spring development capabilities. The application routes notifications through email, SMS, and push channels—a domain that naturally creates:

- Multiple bean implementations requiring smart navigation
- Configuration complexity requiring runtime debugging
- Database queries requiring schema-aware editing

Each scenario tells a **problem → IDE behavior → value** story that flows naturally into the next.

---

## Application Overview

### Domain Model

```
┌─────────────────┐         ┌─────────────────────┐
│  Notification   │         │ NotificationTemplate │
├─────────────────┤         ├─────────────────────┤
│ recipient       │         │ channel             │
│ channel         │────────▶│ templateCode        │
│ templateCode    │         │ subjectTemplate     │
│ payload (Map)   │         │ bodyTemplate        │
└─────────────────┘         │ locale              │
                            │ active              │
                            └─────────────────────┘
```

### Component Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                     NotificationController                        │
│                      POST /api/notify                             │
└───────────────────────────┬──────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│                    NotificationDispatcher                         │
│                                                                   │
│  @Value("${notifyhub.default-channel}")  ◀── Config mystery       │
│  @Value("${notifyhub.rate-limit}")                                │
│                                                                   │
│  List<NotificationSender> senders  ◀── Multiple candidates        │
│  TemplateResolver templateResolver                                │
└───────────────────────────┬──────────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌─────────────────┐ ┌───────────────┐ ┌────────────────┐
│ EmailSender     │ │ SmsSender     │ │ PushSender     │
│ @Service        │ │ @Service      │ │ @Service       │
│                 │ │               │ │ @Primary       │
└─────────────────┘ └───────────────┘ └────────────────┘
```

### Why This Domain

| Landing Page Requirement | How NotifyHub Addresses It |
|--------------------------|---------------------------|
| Multiple bean candidates | Three `NotificationSender` implementations |
| Lombok constructor injection | `@RequiredArgsConstructor` on service classes |
| `@Value` with layered sources | API keys, rate limits from yml + env vars |
| Database queries | Template lookup, audit logging |
| Instantly understandable | Everyone knows what notifications are |
| Professional appearance | Realistic enterprise microservice |

---

## Scenario 1: Spring Support (Bean Navigation & Injection)

### Narrative

> A developer joins the team and opens `NotificationDispatcher` to understand how the notification routing works. They need to quickly grasp which notification channels exist and how Spring wires them together.

### Problem Statement

In a moderately complex Spring application, understanding bean relationships requires:
- Finding all implementations of an interface
- Understanding which beans get injected where
- Navigating through Lombok-generated code
- Resolving ambiguity when multiple candidates exist

Without IDE support, this means grep-ing through files, reading annotations, and mentally tracking `@Primary`, `@Qualifier`, and `@ConditionalOn*` annotations.

### IntelliJ IDEA Behavior to Demonstrate

#### 1.1 Gutter Icon Navigation

**File:** `NotificationDispatcher.java`

```java
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {
    
    private final List<NotificationSender> senders;  // ← Gutter icon here
    private final TemplateResolver templateResolver;
    
    // ...
}
```

**Demonstration:**
- Show the green bean icon in the gutter next to `senders` field
- Click to reveal popup listing all three implementations
- Click through to navigate directly to `EmailNotificationSender`

**Value Statement:** *"One click to see every implementation. No searching, no guessing."*

#### 1.2 Lombok Transparency

**File:** `TemplateResolver.java`

```java
@Service
@RequiredArgsConstructor  // ← Constructor is hidden
@Slf4j
public class TemplateResolver {
    
    private final NotificationTemplateRepository templateRepository;
    private final MessageSource messageSource;
    
    public NotificationTemplate resolve(String code, String locale) {
        // ...
    }
}
```

**Demonstration:**
- No visible constructor, yet IDEA shows injection points
- Gutter icons appear on fields despite Lombok abstraction
- Navigate from `TemplateResolver` usage in `NotificationDispatcher` to its dependencies

**Value Statement:** *"Lombok hides the constructor. IntelliJ IDEA still sees the injection."*

#### 1.3 Single Bean Ambiguity

**File:** `NotificationConfig.java`

```java
@Configuration
public class NotificationConfig {
    
    @Bean
    @ConditionalOnProperty(name = "notifyhub.audit.enabled", havingValue = "true")
    public NotificationAuditor auditor(NotificationSender sender) {  // ← Which sender?
        return new NotificationAuditor(sender);
    }
}
```

**Demonstration:**
- Show warning highlight on `NotificationSender sender` parameter
- Hover to see "Multiple beans of type NotificationSender found"
- Click gutter icon to see all candidates
- Show how adding `@Qualifier("email")` resolves the warning

**Value Statement:** *"Ambiguous injection? Caught at edit time, not runtime."*

#### 1.4 @Primary Resolution

**File:** `PushNotificationSender.java`

```java
@Service
@Primary  // ← This one wins by default
public class PushNotificationSender implements NotificationSender {
    
    @Override
    public void send(Notification notification) {
        // ...
    }
    
    @Override
    public String getChannel() {
        return "push";
    }
}
```

**Demonstration:**
- Show that when a single `NotificationSender` is required, IDEA indicates `@Primary` bean
- Gutter icon shows which bean "wins"
- Contrast with `List<NotificationSender>` which gets all three

**Value Statement:** *"Know which bean wins before you run."*

### Transition Hook

> The developer now understands the bean wiring. But a question emerges: "The dispatcher uses `@Value("${notifyhub.default-channel}")` to determine the fallback channel. What's the actual value in production? It could come from multiple places..."

This uncertainty leads naturally to Scenario 2.

---

## Scenario 2: Debugger with Spring Insights

### Narrative

> The same developer needs to verify configuration values that determine runtime behavior. The `default-channel` property could come from `application.yml`, `application-prod.yml`, environment variables, or command-line arguments. There's no compile-time error—but also no compile-time certainty.

### Problem Statement

Spring's property resolution is powerful but opaque:
- Multiple `@PropertySource` files with override semantics
- Environment variables that override file-based properties
- Profile-specific configurations
- Default values in `@Value` annotations

Reading the code shows *what might happen*. Only runtime reveals *what actually happens*.

### IntelliJ IDEA Behavior to Demonstrate

#### 2.1 Property Source Layering

**Configuration Files:**

```yaml
# application.yml (base)
notifyhub:
  default-channel: email
  rate-limit: 50
  retry:
    max-attempts: 3
    delay-ms: 1000

spring:
  datasource:
    url: jdbc:h2:mem:notifyhub
```

```yaml
# application-prod.yml (profile override)
notifyhub:
  default-channel: push
  rate-limit: 200

spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/notifyhub
```

```bash
# Environment variable (highest priority for this property)
export NOTIFYHUB_RATE_LIMIT=100
```

**Demonstration:**
- Show all three sources defining `rate-limit` with different values
- Explain override order: env var > profile yml > base yml

#### 2.2 @Value at Edit Time

**File:** `NotificationDispatcher.java`

```java
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {
    
    @Value("${notifyhub.default-channel:email}")
    private String defaultChannel;
    
    @Value("${notifyhub.rate-limit:50}")
    private int rateLimit;
    
    @Value("${notifyhub.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    private final List<NotificationSender> senders;
    private final TemplateResolver templateResolver;
    
    public void dispatch(Notification notification) {
        String channel = notification.getChannel() != null 
            ? notification.getChannel() 
            : defaultChannel;  // ← What is this value?
        
        // Rate limiting logic using rateLimit...
        // Retry logic using maxRetryAttempts...
    }
}
```

**Demonstration:**
- Hover over `@Value` annotation
- Show IDEA's property resolution popup (if available)
- Emphasize: "No red squiggles—it compiles. But what's the runtime value?"

#### 2.3 Debugger Session

**Setup:**
- Run application with `prod` profile active
- Set breakpoint inside `dispatch()` method

**Demonstration:**

1. **Hit Breakpoint:**
    - Show execution stopped at `dispatch()` method
    - Variables pane visible

2. **Inspect @Value Fields:**
   ```
   Variables:
   ├── this: NotificationDispatcher@1234
   │   ├── defaultChannel: "push"      ← From application-prod.yml
   │   ├── rateLimit: 100              ← From environment variable!
   │   ├── maxRetryAttempts: 3         ← From application.yml (no override)
   │   ├── senders: ArrayList (size=3)
   │   └── templateResolver: TemplateResolver@5678
   ```

3. **Evaluate Expression (Spring-Aware):**
    - Open "Evaluate Expression" dialog
    - Type: `environment.getProperty("notifyhub.rate-limit")`
    - Show result: `"100"`
    - Type: `environment.getPropertySources()`
    - Show the property source chain

4. **Trace Property Origin:**
    - Demonstrate how to identify which source provided the value
    - Show `ConfigurableEnvironment` inspection

**Value Statement:** *"See the real value. See where it came from. No more println debugging."*

### Positioning Decision

> **Frame as: "Debugger with Spring Insights"**
>
> The standard IntelliJ IDEA debugger becomes Spring-aware when debugging Spring applications. This avoids implying a separate product while highlighting contextual intelligence.
>
> Suggested headline: *"Your debugger understands Spring"*

### Transition Hook

> Configuration is verified—`push` is the default channel in production, rate limit is 100 from the environment. Now the developer needs to check: "Does the template query actually return the right template for the push channel?"

This leads naturally to Scenario 3.

---

## Scenario 3: Database / Datasource Support

### Narrative

> The developer needs to write and verify a query that fetches notification templates by channel. They want to be certain the query matches the actual database schema before running it.

### Problem Statement

Writing SQL or JPQL in Java strings is error-prone:
- No syntax highlighting
- No autocompletion
- Typos discovered only at runtime
- Schema changes break queries silently

Developers often switch to external database tools, losing context and flow.

### IntelliJ IDEA Behavior to Demonstrate

#### 3.1 Datasource Connection

**File:** `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notifyhub
    username: notifyhub_app
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

**Demonstration:**
- Show Database tool window
- Connect to the configured datasource
- Display schema tree: tables, columns, types

**Value Statement:** *"Your IDE knows your schema."*

#### 3.2 Schema Definition

**File:** `schema.sql` (or show in Database tool)

```sql
CREATE TABLE notification_template (
    id              BIGSERIAL PRIMARY KEY,
    channel         VARCHAR(20) NOT NULL,
    template_code   VARCHAR(50) NOT NULL,
    subject_template VARCHAR(200),
    body_template   TEXT NOT NULL,
    locale          VARCHAR(10) DEFAULT 'en',
    active          BOOLEAN DEFAULT true,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP,
    
    CONSTRAINT uk_template_channel_code_locale 
        UNIQUE (channel, template_code, locale)
);

CREATE TABLE notification_log (
    id              BIGSERIAL PRIMARY KEY,
    notification_id UUID NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    template_code   VARCHAR(50),
    status          VARCHAR(20) NOT NULL,
    sent_at         TIMESTAMP,
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_log_recipient (recipient),
    INDEX idx_log_status_sent (status, sent_at)
);
```

#### 3.3 JPQL with Autocompletion

**File:** `NotificationTemplateRepository.java`

```java
@Repository
public interface NotificationTemplateRepository 
        extends JpaRepository<NotificationTemplate, Long> {
    
    // Scenario: Developer types this query
    @Query("SELECT t FROM NotificationTemplate t WHERE t.ch")
                                                      // ↑ Cursor here
}
```

**Demonstration:**

1. **Entity Autocompletion:**
    - Type `SELECT t FROM Notif`
    - Show autocomplete suggesting `NotificationTemplate`

2. **Field Autocompletion:**
    - Type `WHERE t.ch`
    - Show autocomplete suggesting `channel`
    - Show all available fields with types

3. **Complete Query:**
   ```java
   @Query("SELECT t FROM NotificationTemplate t " +
          "WHERE t.channel = :channel AND t.active = true " +
          "ORDER BY t.updatedAt DESC")
   List<NotificationTemplate> findActiveByChannel(@Param("channel") String channel);
   ```

**Value Statement:** *"Autocomplete knows your entities. Typos caught instantly."*

#### 3.4 Native SQL with Schema Awareness

**File:** `NotificationTemplateRepository.java`

```java
@Query(value = """
    SELECT * FROM notification_template 
    WHERE template_code = ?1 
      AND locale = ?2 
      AND active = true
    """, nativeQuery = true)
Optional<NotificationTemplate> findByCodeAndLocale(String code, String locale);
```

**Demonstration:**

1. **Table Name Completion:**
    - Type `FROM notif`
    - Show autocomplete with `notification_template`, `notification_log`

2. **Column Name Completion:**
    - Type `WHERE templ`
    - Show `template_code` suggestion

3. **Typo Detection:**
    - Change `template_code` to `template_cod`
    - Show red squiggle immediately
    - Hover to see "Cannot resolve column 'template_cod'"

4. **Navigate to Schema:**
    - Ctrl+Click on `notification_template`
    - Jump to DDL definition in Database tool

**Value Statement:** *"Write SQL with confidence. Schema errors caught before you run."*

#### 3.5 Query Execution from Editor

**Demonstration:**
- Show "Run Query" gutter icon next to `@Query`
- Execute query directly from repository interface
- View results in tool window

**Value Statement:** *"Test your query without leaving the code."*

### Scenario Resolution

> The developer has now traced the complete flow:
> - **Code:** Understood bean wiring and injection (Scenario 1)
> - **Configuration:** Verified runtime property values (Scenario 2)
> - **Data:** Confirmed query correctness against schema (Scenario 3)
>
> Full confidence from code → config → data, all within the IDE.

---

## Technical Specifications

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.x |
| Build | Maven or Gradle | Latest |
| Database | PostgreSQL (prod) / H2 (dev) | 16 / 2.x |
| Code Gen | Lombok | 1.18.x |
| Testing | JUnit 5 + Testcontainers | Latest |

### Dependencies

```xml
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
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Required IntelliJ IDEA Plugins

- **Spring** (bundled in Ultimate)
- **Database Tools** (bundled in Ultimate)
- **Lombok** (install from marketplace)

---

## File Structure

```
notifyhub-demo/
├── pom.xml
├── README.md
│
├── src/main/java/com/jetbrains/notifyhub/
│   │
│   ├── NotifyHubApplication.java
│   │
│   ├── controller/
│   │   └── NotificationController.java
│   │
│   ├── service/
│   │   ├── NotificationSender.java              # Interface
│   │   ├── EmailNotificationSender.java         # @Service
│   │   ├── SmsNotificationSender.java           # @Service
│   │   ├── PushNotificationSender.java          # @Service @Primary
│   │   ├── NotificationDispatcher.java          # Main orchestrator
│   │   └── TemplateResolver.java                # @RequiredArgsConstructor
│   │
│   ├── config/
│   │   ├── NotificationConfig.java              # @Configuration
│   │   └── NotificationProperties.java          # @ConfigurationProperties (optional)
│   │
│   ├── model/
│   │   ├── Notification.java                    # Request DTO
│   │   ├── NotificationTemplate.java            # JPA Entity
│   │   ├── NotificationLog.java                 # JPA Entity
│   │   └── NotificationResult.java              # Response DTO
│   │
│   ├── repository/
│   │   ├── NotificationTemplateRepository.java
│   │   └── NotificationLogRepository.java
│   │
│   └── audit/
│       └── NotificationAuditor.java             # For ambiguity demo
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-prod.yml
│   ├── application-dev.yml
│   └── schema.sql
│
└── src/test/java/com/jetbrains/notifyhub/
    ├── NotifyHubApplicationTests.java
    └── repository/
        └── NotificationTemplateRepositoryTest.java
```

---

## Screenshot Capture Guide

### General Setup

1. **Theme:** Use default IntelliJ Light or Darcula (choose one for consistency)
2. **Font Size:** Increase editor font to 14-16pt for readability
3. **Window Size:** 1920x1080 minimum, show only relevant panels
4. **Hide Distractions:** Close unnecessary tool windows

### Scenario 1 Screenshots

| # | Shot Description | File Open | UI State |
|---|------------------|-----------|----------|
| 1.1 | Bean gutter icons overview | `NotificationDispatcher.java` | Editor with gutter visible, cursor on `senders` field |
| 1.2 | Bean candidates popup | `NotificationDispatcher.java` | Click gutter icon, show popup with 3 implementations |
| 1.3 | Navigation to implementation | `EmailNotificationSender.java` | After clicking through from popup |
| 1.4 | Lombok class with injection markers | `TemplateResolver.java` | Show gutter icons on fields despite no constructor |
| 1.5 | Ambiguity warning | `NotificationConfig.java` | Yellow highlight on ambiguous parameter |
| 1.6 | Ambiguity candidates popup | `NotificationConfig.java` | Gutter click showing candidates |
| 1.7 | @Primary indicator | `PushNotificationSender.java` | Show @Primary annotation with gutter indication |

### Scenario 2 Screenshots

| # | Shot Description | File Open | UI State |
|---|------------------|-----------|----------|
| 2.1 | Multiple config files | Project view | Show application.yml and application-prod.yml |
| 2.2 | @Value annotations | `NotificationDispatcher.java` | Editor showing @Value fields |
| 2.3 | Debugger breakpoint hit | `NotificationDispatcher.java` | Debug mode, execution paused |
| 2.4 | Variables pane with values | Debug tool window | Expanded `this` showing resolved @Value fields |
| 2.5 | Evaluate expression | Evaluate dialog | Showing `environment.getProperty()` result |
| 2.6 | Property sources chain | Evaluate dialog | Showing property source hierarchy |

### Scenario 3 Screenshots

| # | Shot Description | File Open | UI State |
|---|------------------|-----------|----------|
| 3.1 | Database tool connected | Database tool window | Schema tree expanded showing tables |
| 3.2 | JPQL entity autocomplete | `NotificationTemplateRepository.java` | Autocomplete popup for entity name |
| 3.3 | JPQL field autocomplete | `NotificationTemplateRepository.java` | Autocomplete popup for `t.channel` |
| 3.4 | Native SQL table autocomplete | `NotificationTemplateRepository.java` | Autocomplete in native query |
| 3.5 | Schema error detection | `NotificationTemplateRepository.java` | Red squiggle on typo in column name |
| 3.6 | Navigate to DDL | Database tool window | DDL view after Ctrl+Click on table |
| 3.7 | Run query from editor | `NotificationTemplateRepository.java` | Gutter run icon, results in tool window |

### Screencast Storyboard (Optional)

If creating video content:

```
00:00 - 00:30  |  Opening: Show project structure, introduce NotifyHub
00:30 - 02:00  |  Scenario 1: Navigate beans, show Lombok, resolve ambiguity
02:00 - 02:15  |  Transition: "But what value does this config have at runtime?"
02:15 - 03:30  |  Scenario 2: Debug session, inspect @Value, evaluate expressions
03:30 - 03:45  |  Transition: "Config is right. Does the query match the schema?"
03:45 - 05:00  |  Scenario 3: Write query with autocomplete, catch typo, run query
05:00 - 05:15  |  Closing: Recap value proposition
```

---

## Appendix: Key Code Snippets for Screenshots

### NotificationSender Interface

```java
public interface NotificationSender {
    
    void send(Notification notification);
    
    String getChannel();
    
    default boolean supports(String channel) {
        return getChannel().equalsIgnoreCase(channel);
    }
}
```

### NotificationDispatcher (Primary Demo File)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {
    
    @Value("${notifyhub.default-channel:email}")
    private String defaultChannel;
    
    @Value("${notifyhub.rate-limit:50}")
    private int rateLimit;
    
    @Value("${notifyhub.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    private final List<NotificationSender> senders;
    private final TemplateResolver templateResolver;
    
    public NotificationResult dispatch(Notification notification) {
        String channel = notification.getChannel() != null 
            ? notification.getChannel() 
            : defaultChannel;
        
        log.info("Dispatching notification to channel: {}", channel);
        
        NotificationSender sender = senders.stream()
            .filter(s -> s.supports(channel))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "No sender found for channel: " + channel));
        
        NotificationTemplate template = templateResolver.resolve(
            notification.getTemplateCode(),
            notification.getLocale()
        );
        
        // Apply template and send...
        sender.send(notification);
        
        return NotificationResult.success(notification.getId());
    }
}
```

### Repository with Queries

```java
@Repository
public interface NotificationTemplateRepository 
        extends JpaRepository<NotificationTemplate, Long> {
    
    @Query("SELECT t FROM NotificationTemplate t " +
           "WHERE t.channel = :channel AND t.active = true " +
           "ORDER BY t.updatedAt DESC")
    List<NotificationTemplate> findActiveByChannel(
        @Param("channel") String channel
    );
    
    @Query(value = """
        SELECT * FROM notification_template 
        WHERE template_code = ?1 
          AND locale = ?2 
          AND active = true
        """, nativeQuery = true)
    Optional<NotificationTemplate> findByCodeAndLocale(
        String code, 
        String locale
    );
    
    @Query("SELECT t FROM NotificationTemplate t " +
           "WHERE t.templateCode = :code")
    List<NotificationTemplate> findAllByCode(
        @Param("code") String code
    );
}
```

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-01 | — | Initial specification |

---

*This document serves as the specification for the NotifyHub demo application. Implementation should follow these guidelines to ensure consistent demonstration of IntelliJ IDEA's Spring development capabilities.*
