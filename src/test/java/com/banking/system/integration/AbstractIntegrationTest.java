package com.banking.system.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests with optimized TestContainers setup.
 *
 * <p>Uses static initializer (not @Container) so the container lives for the
 * entire JVM lifecycle and is shared across ALL test classes.
 *
 * <p>Usage: Extend this class in your integration test classes:
 * <pre>
 * class MyServiceIT extends AbstractIntegrationTest {
 *     // Your tests here
 * }
 * </pre>
 */
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
     * Started via static initializer so the container survives across test classes.
     * Using @Container would let @Testcontainers stop the container after each
     * test class, breaking subsequent classes that reuse the Spring context.
     */
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName(TEST_DB_NAME)
                    .withUsername(TEST_USERNAME)
                    .withPassword(TEST_PASSWORD)
                    .withReuse(true);

    static {
        postgres.start();
    }

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
