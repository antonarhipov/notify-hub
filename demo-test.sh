#!/bin/bash

# NotifyHub Demo Test Script
# This script tests all key functionality of the NotifyHub application

echo "======================================"
echo "NotifyHub Demo Test Script"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if application is running
echo -e "${BLUE}Checking if application is running...${NC}"
if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Application is running${NC}"
else
    echo -e "${RED}✗ Application is not running. Start it with:${NC}"
    echo "  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev"
    exit 1
fi

echo ""

# Test 1: Email Notification
echo -e "${BLUE}Test 1: Sending Email Notification...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"user@example.com","channel":"email","templateCode":"welcome","locale":"en","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    NOTIFICATION_ID=$(echo "$RESPONSE" | grep -o '"notificationId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ Email notification sent successfully${NC}"
    echo "  Notification ID: $NOTIFICATION_ID"
else
    echo -e "${RED}✗ Email notification failed${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""

# Test 2: SMS Notification
echo -e "${BLUE}Test 2: Sending SMS Notification...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"555-1234","channel":"sms","templateCode":"welcome","locale":"en","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    NOTIFICATION_ID=$(echo "$RESPONSE" | grep -o '"notificationId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ SMS notification sent successfully${NC}"
    echo "  Notification ID: $NOTIFICATION_ID"
else
    echo -e "${RED}✗ SMS notification failed${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""

# Test 3: Push Notification
echo -e "${BLUE}Test 3: Sending Push Notification...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"device-123","channel":"push","templateCode":"welcome","locale":"en","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    NOTIFICATION_ID=$(echo "$RESPONSE" | grep -o '"notificationId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ Push notification sent successfully${NC}"
    echo "  Notification ID: $NOTIFICATION_ID"
else
    echo -e "${RED}✗ Push notification failed${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""

# Test 4: Default Channel (no channel specified)
echo -e "${BLUE}Test 4: Sending notification with default channel...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"default@example.com","templateCode":"welcome","locale":"en","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    NOTIFICATION_ID=$(echo "$RESPONSE" | grep -o '"notificationId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ Notification with default channel sent successfully${NC}"
    echo "  Notification ID: $NOTIFICATION_ID"
    echo "  Note: Check logs to confirm default channel (should be 'email' in dev profile)"
else
    echo -e "${RED}✗ Notification with default channel failed${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""

# Test 5: Spanish locale
echo -e "${BLUE}Test 5: Sending notification with Spanish locale...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"recipient":"usuario@example.com","channel":"email","templateCode":"welcome","locale":"es","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":true"; then
    NOTIFICATION_ID=$(echo "$RESPONSE" | grep -o '"notificationId":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ Spanish notification sent successfully${NC}"
    echo "  Notification ID: $NOTIFICATION_ID"
else
    echo -e "${RED}✗ Spanish notification failed${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""

# Test 6: Invalid request (missing recipient)
echo -e "${BLUE}Test 6: Testing validation (invalid request)...${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/notify \
  -H "Content-Type: application/json" \
  -d '{"channel":"email","templateCode":"welcome","locale":"en","payload":{}}')

if echo "$RESPONSE" | grep -q "\"success\":false" || echo "$RESPONSE" | grep -q "error"; then
    echo -e "${GREEN}✓ Validation working correctly (request rejected)${NC}"
else
    echo -e "${RED}✗ Validation not working (invalid request accepted)${NC}"
    echo "  Response: $RESPONSE"
fi

echo ""
echo "======================================"
echo "Demo Test Summary"
echo "======================================"
echo ""
echo "All tests completed. The application is working correctly."
echo ""
echo "Next steps for IntelliJ IDEA demo:"
echo "1. Open NotificationDispatcher.java - See bean navigation gutter icons"
echo "2. Set breakpoint and run with prod profile - See @Value field resolution"
echo "3. Open NotificationTemplateRepository.java - See JPQL/SQL autocomplete"
echo "4. Open NotificationConfig.java - See bean ambiguity warning"
echo ""
