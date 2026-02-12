package com.example.notifyhub;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for integration tests.
 *
 * Uses H2 in-memory database by default (configured in application-test.yml).
 *
 * For Testcontainers demo (when Docker is available):
 * - Uncomment @Testcontainers annotation
 * - Uncomment the PostgreSQL container field
 * - Add required imports
 *
 * This demonstrates IntelliJ IDEA's Testcontainers support and Spring Boot 3.1+ @ServiceConnection.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    /*
     * Uncomment for Testcontainers demo (requires Docker):
     *
     * @Container
     * @ServiceConnection
     * static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
     *         .withDatabaseName("notifyhub_test")
     *         .withUsername("test")
     *         .withPassword("test");
     */
}
