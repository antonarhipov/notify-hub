# Demo Markers Guide

This document lists all `//DEMO` comments in the codebase that mark key demonstration points for IntelliJ IDEA features.

## Quick Reference

### 1. Bean Navigation & Injection

#### NotificationDispatcher.java
- **Line 54-55**: `//DEMO Demonstrates bean navigation - gutter icon shows 3 implementations`
  - Click gutter icon on `List<NotificationSender> senders` field
  - Shows: EmailNotificationSender, SmsNotificationSender, PushNotificationSender

- **Line 84**: `//DEMO Bean Navigation - Click gutter icon on 'senders' to see all 3 implementations`
  - Demonstrates runtime usage of injected list

#### NotificationSender.java (Interface)
- **Line 7-8**: `//DEMO Bean Navigation - Click gutter icon on interface name to see 3 implementations`
  - Shows all implementing classes

#### EmailNotificationSender.java
- **Line 7-8**: `//DEMO Bean Navigation - Gutter icon on class shows it implements NotificationSender`
  - Navigate from implementation to interface

#### PushNotificationSender.java
- **Line 7-8**: `//DEMO @Primary Bean - This bean is the default choice when NotificationSender is injected`
  - Shows @Primary bean handling

#### NotificationConfig.java
- **Line 20-23**: `//DEMO Bean Ambiguity - Yellow warning on 'sender' parameter`
  - Click gutter icon to see 3 candidate beans
  - Shows how @Primary affects default selection
  - Fix with @Qualifier annotation

#### TemplateResolver.java
- **Line 7-9**: `//DEMO Lombok @RequiredArgsConstructor - No visible constructor in code`
  - Gutter icons on fields show dependency injection
  - IDE understands Lombok-generated constructor

---

### 2. Debugger with Spring Insights

#### NotificationDispatcher.java
- **Line 44-46**: `//DEMO Debugger Insights - Set breakpoint at line 70 and inspect these @Value fields`
  - With prod profile: `defaultChannel="push"`, `rateLimit=200`
  - With dev profile: `defaultChannel="email"`, `rateLimit=50`
  - Shows profile-based property resolution

- **Line 71**: `//DEMO Set breakpoint on the next line to inspect @Value fields in debugger`
  - Breakpoint location for observing field values
  - Inspect: `defaultChannel`, `rateLimit`, `maxRetryAttempts`

---

### 3. Database Tooling

#### NotificationTemplateRepository.java

##### JPQL Query Demo
- **Line 20-22**: `//DEMO Database Tooling - JPQL Query with Entity Autocomplete`
  - Type: `"SELECT t FROM Notif"` → see NotificationTemplate suggested
  - Type: `"WHERE t.ch"` → see 'channel' field suggested
  - Entity and field autocomplete

##### Native SQL Query Demo
- **Line 33-37**: `//DEMO Database Tooling - Native SQL Query with Table/Column Autocomplete`
  - Type: `"FROM notif"` → see 'notification_template' table suggested
  - Type: `"WHERE temp"` → see 'template_code' column suggested
  - Introduce typo like `"template_cod"` → see red squiggle validation error
  - Ctrl+Click on 'notification_template' → navigate to schema.sql

---

## Demo Workflow

### Scenario 1: Bean Navigation & Dependency Injection

1. **Open**: `NotificationSender.java`
   - Click gutter icon on interface → see 3 implementations

2. **Open**: `NotificationDispatcher.java` (line 54-55)
   - Click gutter icon on `List<NotificationSender> senders` → see all implementations
   - Observe `@RequiredArgsConstructor` with no visible constructor

3. **Open**: `TemplateResolver.java` (line 7-9)
   - See Lombok `@RequiredArgsConstructor` demo
   - Gutter icons on fields show injection

4. **Open**: `EmailNotificationSender.java` (line 7-8)
   - Click gutter icon → navigate to interface

5. **Open**: `PushNotificationSender.java` (line 7-8)
   - See `@Primary` annotation
   - Understand default bean selection

6. **Open**: `NotificationConfig.java` (line 20-23)
   - See yellow warning on ambiguous parameter
   - Click gutter icon → see 3 candidates
   - Note that @Primary would select PushNotificationSender

---

### Scenario 2: Debugger with Spring Insights

1. **Open**: `NotificationDispatcher.java`
   - Read //DEMO comments at lines 44-46
   - Set breakpoint at line 71

2. **Run with dev profile**:
   ```bash
   --spring.profiles.active=dev
   ```
   - Send POST request to `/api/notify`
   - In debugger, inspect:
     - `defaultChannel` = "email"
     - `rateLimit` = 50
     - `maxRetryAttempts` = 3

3. **Run with prod profile**:
   ```bash
   --spring.profiles.active=prod
   ```
   - Send POST request to `/api/notify`
   - In debugger, inspect:
     - `defaultChannel` = "push" (from application-prod.yml)
     - `rateLimit` = 200 (from application-prod.yml)
     - `maxRetryAttempts` = 3 (from base application.yml)

4. **Verify**:
   - Evaluate expression: `this.defaultChannel` in debugger
   - Shows resolved values from active profile

---

### Scenario 3: Database Tooling

1. **Open**: `NotificationTemplateRepository.java`

2. **JPQL Demo** (line 20-22):
   - Place cursor in @Query string
   - Type `"SELECT t FROM Notif"` → autocomplete suggests `NotificationTemplate`
   - Type `"WHERE t.ch"` → autocomplete suggests `channel`
   - Try other fields: `active`, `templateCode`, `locale`, `updatedAt`

3. **Native SQL Demo** (line 33-37):
   - Place cursor in @Query string
   - Type `"FROM notif"` → autocomplete suggests `notification_template`
   - Type `"WHERE temp"` → autocomplete suggests `template_code`
   - Try other columns: `locale`, `active`, `channel`, `body_template`

4. **Schema Validation**:
   - Introduce typo: `"WHERE template_cod"` → see red squiggle
   - Error message: "Cannot resolve column 'template_cod'"

5. **Navigation**:
   - Ctrl+Click on `notification_template` table name
   - Navigate to `schema.sql` DDL definition

6. **Database Tool Window**:
   - Connect to H2 datasource
   - Browse schema: see tables, columns, types, constraints
   - Right-click table → Jump to DDL

---

## All //DEMO Locations

| File | Line | Feature | Description |
|------|------|---------|-------------|
| NotificationDispatcher.java | 44-46 | Debugger Insights | @Value field resolution with profiles |
| NotificationDispatcher.java | 54-55 | Bean Navigation | List<NotificationSender> injection |
| NotificationDispatcher.java | 71 | Debugger Insights | Breakpoint location |
| NotificationDispatcher.java | 84 | Bean Navigation | Runtime usage of senders list |
| NotificationSender.java | 7-8 | Bean Navigation | Interface with multiple implementations |
| EmailNotificationSender.java | 7-8 | Bean Navigation | Implementation to interface navigation |
| PushNotificationSender.java | 7-8 | Bean Navigation | @Primary bean handling |
| NotificationConfig.java | 20-23 | Bean Ambiguity | Multiple candidates warning |
| TemplateResolver.java | 7-9 | Lombok | @RequiredArgsConstructor demo |
| NotificationTemplateRepository.java | 20-22 | Database | JPQL autocomplete |
| NotificationTemplateRepository.java | 33-37 | Database | Native SQL autocomplete |

---

## Testing the Demo

### Quick Test
```bash
# Start application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run demo test script
./demo-test.sh
```

### Manual Testing
```bash
# Test email notification
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"user@example.com","channel":"email","templateCode":"welcome","locale":"en","payload":{}}'

# Test SMS notification
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"555-1234","channel":"sms","templateCode":"welcome","locale":"en","payload":{}}'

# Test default channel (should use dev profile default: email)
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"default@example.com","templateCode":"welcome","locale":"en","payload":{}}'
```

---

## Key Files Summary

**Primary Demo File**: `NotificationDispatcher.java`
- Demonstrates all 3 scenarios in one class
- Most comprehensive demo file

**Bean Navigation Files**:
- `NotificationSender.java` (interface)
- `EmailNotificationSender.java`
- `SmsNotificationSender.java`
- `PushNotificationSender.java`
- `NotificationConfig.java` (ambiguity)
- `TemplateResolver.java` (Lombok)

**Debugger Files**:
- `NotificationDispatcher.java` (@Value fields)
- `application.yml` (base config)
- `application-dev.yml` (dev overrides)
- `application-prod.yml` (prod overrides)

**Database Files**:
- `NotificationTemplateRepository.java` (queries)
- `schema.sql` (DDL)
- `data.sql` (sample data)
