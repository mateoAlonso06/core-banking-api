package com.banking.system.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with optimized TestContainers setup.
 *
 * <p>Key optimizations:
 * <ul>
 *   <li>Singleton container shared across all test classes (static final)</li>
 *   <li>Container reuse enabled via testcontainers.properties</li>
 *   <li>Lightweight postgres:16-alpine image</li>
 * </ul>
 *
 * <p>Usage: Extend this class in your integration test classes:
 * <pre>
 * class MyServiceIT extends AbstractIntegrationTest {
 *     // Your tests here
 * }
 * </pre>
 *
 * <p>Performance impact:
 * <ul>
 *   <li>First test run: ~10-15 seconds (container startup)</li>
 *   <li>Subsequent test runs: ~2-3 seconds (container reused)</li>
 * </ul>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static final String TEST_DB_NAME = "testdb";
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";

    /**
     * Singleton PostgreSQL container shared across all integration tests.
     *
     * The container is started once and reused across all test classes thanks to:
     * 1. Static final declaration (singleton pattern)
     * 2. withReuse(true) configuration
     * 3. testcontainers.reuse.enable=true property
     *
     * Note: The container will be stopped automatically when the JVM exits,
     * but can be reused in subsequent test runs if configured properly.
     */
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(TEST_DB_NAME)
                    .withUsername(TEST_USERNAME)
                    .withPassword(TEST_PASSWORD)
                    .withReuse(true); // Enable container reuse between test runs

    /**
     * Configures Spring datasource properties dynamically from the container.
     * This method is called once per test class before the Spring context is loaded.
     */
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Optimize Flyway for tests
        registry.add("spring.flyway.clean-disabled", () -> "false");

        // Disable unnecessary features in tests
        registry.add("spring.jpa.show-sql", () -> "false");
    }
}
