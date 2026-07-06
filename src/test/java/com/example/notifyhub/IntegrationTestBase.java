package com.example.notifyhub;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests.
 *
 * Spins up a real PostgreSQL database using Testcontainers and wires it into the
 * Spring application context via Spring Boot's {@link ServiceConnection} support,
 * so Flyway migrations run against the same engine used in production.
 *
 * <p>The container follows the singleton pattern: it is started once in a static
 * initializer and kept running for the whole JVM (cleaned up by Ryuk on exit).
 * This keeps it alive across the multiple cached Spring contexts used by the test
 * suite, avoiding stale-connection errors that occur when the container is stopped
 * between test classes.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notifyhub_test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }
}
