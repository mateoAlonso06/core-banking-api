# Testing Strategy

This document explains the testing strategy and optimizations implemented in this project.

## Overview

The project uses a **two-tier testing strategy** to balance speed and confidence:

1. **Unit Tests** - Fast, isolated tests (milliseconds)
2. **Integration Tests** - Full stack tests with TestContainers (seconds)

## Quick Reference

```bash
# Development (fast feedback loop)
mvn clean verify                          # Unit tests only (~1-2 min)

# Before commit (verify everything works)
mvn clean verify -Pintegration-tests      # Integration tests only (~10-15 sec)

# CI/CD pipeline (complete validation)
mvn clean verify -Pall-tests              # Both unit and integration tests
```

## Test Profiles

### 1. `unit-tests` (Default Profile)

**Runs:** Unit tests only (`*Test.java`)
**Skips:** Integration tests (`*IT.java`)
**Speed:** Fast (1-2 minutes)
**Use case:** Development, quick feedback

```bash
mvn clean verify
# or explicitly:
mvn clean verify -Punit-tests
```

### 2. `integration-tests`

**Runs:** Integration tests only (`*IT.java`)
**Skips:** Unit tests (`*Test.java`)
**Speed:** Moderate (30-60 sec first run, 10-15 sec with reuse)
**Use case:** Pre-commit verification, integration validation

```bash
mvn clean verify -Pintegration-tests
```

### 3. `all-tests`

**Runs:** Both unit and integration tests
**Speed:** Combined (2-3 minutes)
**Use case:** CI/CD, release validation

```bash
mvn clean verify -Pall-tests
```

## Naming Convention

| Test Type        | Suffix Pattern | Example                      | Profile            |
|-----------------|----------------|------------------------------|-------------------|
| Unit Test       | `*Test.java`   | `CustomerServiceTest.java`   | `unit-tests`      |
| Integration Test| `*IT.java`     | `AuthRestControllerIT.java`  | `integration-tests`|

## TestContainers Optimization

Integration tests use [TestContainers](https://www.testcontainers.org/) with several optimizations:

### 1. Singleton Pattern

All integration tests extend `AbstractIntegrationTest`, which creates a **single shared container**:

```java
@Container
private static final PostgreSQLContainer<?> postgres = // ...
```

Benefits:
- Container started once per test suite
- Shared across all test classes
- Reduces startup overhead

### 2. Container Reuse

Enabled via `src/test/resources/testcontainers.properties`:

```properties
testcontainers.reuse.enable=true
```

Benefits:
- Container persists between Maven runs
- First run: ~30-60 seconds (cold start)
- Subsequent runs: ~10-15 seconds (warm start)
- Docker daemon keeps container alive

### 3. Lightweight Image

Uses `postgres:16-alpine` instead of full Postgres:
- Smaller image size (~80MB vs ~350MB)
- Faster download and startup
- Same functionality for tests

### Performance Comparison

| Scenario                    | Without Optimization | With Optimization |
|-----------------------------|---------------------|------------------|
| First run (cold start)      | ~60-90 seconds      | ~30-60 seconds   |
| Subsequent runs             | ~60-90 seconds      | ~10-15 seconds   |
| Per test class overhead     | ~10-15 seconds      | ~1-2 seconds     |

## Writing Tests

### Unit Test Example

```java
// File: CustomerServiceTest.java
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService service;

    @Test
    void shouldCreateCustomer() {
        // Fast, no dependencies
        // Tests business logic in isolation
    }
}
```

### Integration Test Example

```java
// File: CustomerRestControllerIT.java
class CustomerRestControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterCustomerSuccessfully() throws Exception {
        // Full stack: Controller → Service → Repository → Database
        // Uses real PostgreSQL container
        mockMvc.perform(post("/api/v1/customers")
            .content(validRequest))
            .andExpect(status().isCreated());
    }
}
```

## Best Practices

### 1. Test Pyramid

Maintain a healthy test pyramid:

```
        /\
       /  \      10-20% - Integration Tests (IT)
      /____\
     /      \
    /        \   80-90% - Unit Tests (Test)
   /__________\
```

### 2. When to Write Each Type

**Unit Tests:**
- Business logic validation
- Domain model behavior
- Service layer orchestration
- Mapper transformations
- Edge cases and error handling

**Integration Tests:**
- REST endpoint behavior
- Database transactions
- Security configuration
- Full request/response flows
- Cross-layer integration

### 3. Keep Integration Tests Focused

```java
// Good: Tests one specific scenario
@Test
void shouldFailLoginWithInvalidCredentials() { /* ... */ }

// Bad: Tests multiple scenarios in one test
@Test
void testLoginScenarios() { /* 10 different scenarios */ }
```

### 4. Clean Up Test Data

```java
@AfterEach
void cleanUp() {
    // Clean up test data to avoid test pollution
    customerRepository.deleteAll();
}
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: CI

on: [push, pull_request]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          cache: 'maven'
      - name: Run unit tests
        run: mvn clean verify -Punit-tests

  integration-tests:
    runs-on: ubuntu-latest
    # Only on main/develop or manual trigger
    if: github.ref == 'refs/heads/main' || github.event_name == 'workflow_dispatch'
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
          cache: 'maven'
      - name: Run integration tests
        run: mvn clean verify -Pintegration-tests
```

## Troubleshooting

### Container Not Reusing

If containers aren't being reused:

1. Check `testcontainers.properties` exists in `src/test/resources/`
2. Verify `testcontainers.reuse.enable=true` is set
3. Ensure `withReuse(true)` is called on container
4. Check Docker daemon is running

### Tests Running Slowly

If integration tests are slow:

1. Verify container reuse is enabled
2. Check you're using `postgres:16-alpine` (not full postgres)
3. Ensure singleton pattern in `AbstractIntegrationTest`
4. Look for unnecessary `@DirtiesContext` annotations

### Profile Not Working

If wrong tests are running:

1. Verify profile name: `-Punit-tests`, `-Pintegration-tests`, `-Pall-tests`
2. Check test naming: `*Test.java` vs `*IT.java`
3. Run with `-X` flag for debug output: `mvn clean verify -Punit-tests -X`

## Further Reading

- [TestContainers Documentation](https://www.testcontainers.org/)
- [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/)
- [Spring Boot Testing Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)