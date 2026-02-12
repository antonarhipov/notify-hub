# Quick Demo Guide - NotifyHub

## 🎯 3-Minute Demo Script

### Setup (30 seconds)
```bash
# Terminal 1: Start application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Test it's running
curl http://localhost:8080/api/health
# Should return: "NotifyHub is running"
```

---

## Demo 1: Bean Navigation (60 seconds)

### Step 1: Multiple Implementations
**File**: `NotificationDispatcher.java` → Line 54-55

```java
//DEMO Demonstrates bean navigation - gutter icon shows 3 implementations
private final List<NotificationSender> senders;
```

**Action**: Click gutter icon on `senders`
**Result**: Shows 3 implementations (Email, SMS, Push)

### Step 2: Interface to Implementations
**File**: `NotificationSender.java` → Line 7-8

**Action**: Click gutter icon on interface name
**Result**: Navigate to all implementations

### Step 3: Bean Ambiguity
**File**: `NotificationConfig.java` → Line 20-23

**Action**: Hover over yellow warning on `sender` parameter
**Result**: Shows 3 candidates, @Primary bean highlighted

---

## Demo 2: Debugger Insights (60 seconds)

### Setup
**File**: `NotificationDispatcher.java` → Line 71

1. Set breakpoint on line 71 (log.info statement)
2. Run with Debug mode
3. Send test request

### Test Request
```bash
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"user@example.com","channel":"email","templateCode":"welcome","locale":"en","payload":{}}'
```

### Observe in Debugger
**Variables Pane**:
- `defaultChannel` = **"email"** (dev profile)
- `rateLimit` = **50** (dev profile)
- `maxRetryAttempts` = **3** (base config)

### Now Try Prod Profile
Stop app, restart with:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Same breakpoint, observe:
- `defaultChannel` = **"push"** (prod override!)
- `rateLimit` = **200** (prod override!)
- `maxRetryAttempts` = **3** (unchanged)

---

## Demo 3: Database Tooling (60 seconds)

### Step 1: JPQL Autocomplete
**File**: `NotificationTemplateRepository.java` → Line 20-22

**Query**:
```java
@Query("SELECT t FROM NotificationTemplate t WHERE t.channel = :channel AND t.active = true")
```

**Try Editing**:
1. Type: `"SELECT t FROM Notif"` → autocomplete suggests `NotificationTemplate`
2. Type: `"WHERE t.ch"` → autocomplete suggests `channel`
3. Try: `t.active`, `t.templateCode`, `t.locale`

### Step 2: Native SQL Autocomplete
**File**: `NotificationTemplateRepository.java` → Line 33-37

**Query**:
```sql
SELECT * FROM notification_template WHERE template_code = :code
```

**Try Editing**:
1. Type: `"FROM notif"` → autocomplete suggests `notification_template`
2. Type: `"WHERE temp"` → autocomplete suggests `template_code`
3. Try typo: `"WHERE template_cod"` → red squiggle!

### Step 3: Navigate to Schema
**Action**: Ctrl+Click on `notification_template` in query
**Result**: Jumps to `schema.sql` DDL definition

---

## 🎬 Complete Demo Flow (3 minutes)

### Start
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Demo Order
1. **Bean Navigation** (1 min)
   - `NotificationDispatcher.java:54` - gutter icon
   - `NotificationConfig.java:20` - ambiguity warning

2. **Debugger** (1 min)
   - Breakpoint at `NotificationDispatcher.java:71`
   - Send request, inspect @Value fields
   - Show dev vs prod difference

3. **Database** (1 min)
   - `NotificationTemplateRepository.java:20` - JPQL autocomplete
   - `NotificationTemplateRepository.java:33` - SQL autocomplete
   - Navigate to schema

---

## 📝 Talking Points

### Bean Navigation
"IntelliJ IDEA shows gutter icons for dependency injection. Here we have 3 implementations of NotificationSender, and the IDE lets us navigate between them instantly. Notice the @Primary annotation - the IDE highlights this as the default choice when there's ambiguity."

### Debugger Insights
"With Spring Boot's profile system, properties can override each other. The debugger shows the resolved values. In dev mode, defaultChannel is 'email' with rate limit 50. Switch to prod, and it changes to 'push' with rate limit 200. The IDE understands where these values come from."

### Database Tooling
"IntelliJ IDEA provides full autocomplete for JPQL and SQL queries. It knows your entity fields and database columns. Typos are caught immediately with red squiggles. You can even navigate from queries to schema definitions."

---

## 🚀 Quick Commands

```bash
# Build
./mvnw clean package

# Run dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run prod (requires PostgreSQL)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Run tests
./mvnw test

# Test API
curl -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"user@example.com","channel":"email","templateCode":"welcome","locale":"en","payload":{}}'

# Auto demo test
./demo-test.sh
```

---

## 📍 Key Files Locations

| Feature | File | Line |
|---------|------|------|
| Bean Navigation | NotificationDispatcher.java | 54 |
| Debugger Breakpoint | NotificationDispatcher.java | 71 |
| Bean Ambiguity | NotificationConfig.java | 20 |
| JPQL Query | NotificationTemplateRepository.java | 20 |
| SQL Query | NotificationTemplateRepository.java | 33 |

---

## 🔍 Search for //DEMO

All demo points are marked with `//DEMO` comments:

```bash
# Find all demo markers
grep -r "//DEMO" src/
```

Or in IntelliJ: **Ctrl+Shift+F** → Search for `//DEMO`

---

## 📊 Expected Results

### Bean Navigation
✅ Gutter icons visible
✅ Navigate to 3 implementations
✅ Yellow warning on ambiguous parameter

### Debugger
✅ Dev profile: defaultChannel="email", rateLimit=50
✅ Prod profile: defaultChannel="push", rateLimit=200

### Database
✅ Entity autocomplete works
✅ Table/column autocomplete works
✅ Typo detection with red squiggle
✅ Navigate to schema.sql

---

## 🐛 Troubleshooting

**App won't start**
- Check Java version: `java -version` (need 21+)
- Check port: `lsof -i :8080`

**Debugger doesn't show @Value resolution**
- Ensure Spring Boot DevTools is not interfering
- Use proper debug configuration (not just run)
- Check active profile is correct

**Database autocomplete not working**
- Connect to database in Database tool window
- Ensure data source is configured
- Refresh database schema

**//DEMO comments not visible**
- Search for `//DEMO` in project
- Check you're in the correct file
- Use "Find in Files" (Ctrl+Shift+F)

---

## ✨ Bonus Tips

1. **Show Source Code**: Always show the `//DEMO` comments when presenting
2. **Live Coding**: Try autocomplete live, don't just show finished code
3. **Intentional Errors**: Introduce typos to show validation
4. **Navigation**: Use Ctrl+Click to jump around and show navigation
5. **Compare**: Show application-dev.yml vs application-prod.yml side by side

---

## 🎓 Learning Resources

- Full documentation: `README.md`
- Implementation details: `IMPLEMENTATION_SUMMARY.md`
- All demo markers: `DEMO_MARKERS.md`
- Project structure: See package tree in IntelliJ

---

**Ready to demo? Start with Bean Navigation - it's the most visual!**
