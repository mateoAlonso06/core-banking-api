# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Test
```bash
# Clean and compile
mvn clean compile

# Run all tests
mvn test

# Run tests with coverage report (JaCoCo)
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=ClassName

# Run specific test method
mvn test -Dtest=ClassName#methodName

# Package application
mvn clean package

# Skip tests during packaging
mvn clean package -DskipTests
```

### Running the Application

**Local (requires PostgreSQL running):**
```bash
mvn spring-boot:run
```

**Docker Compose (recommended):**
```bash
# Start all services (app + PostgreSQL)
docker-compose up --build

# Start in detached mode
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes (clean database)
docker-compose down -v
```

### Database Migrations
- Uses **Flyway** for database migrations
- Migration files: `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1__init_migration.sql`)
- Migrations run automatically on application startup
- `spring.jpa.hibernate.ddl-auto=validate` ensures schema matches migrations

## Architecture

### Hexagonal Architecture (Ports & Adapters)

The codebase follows hexagonal architecture with clear separation of concerns:

**Module Structure:**
```
<module>/
├── domain/               # Business logic (framework-independent)
│   ├── model/           # Domain entities with business rules
│   ├── port/
│   │   ├── in/         # Use case interfaces (inbound ports)
│   │   └── out/        # Repository/service interfaces (outbound ports)
│   └── exception/       # Domain-specific exceptions
├── application/          # Application services (orchestration)
│   ├── service/         # Implements use cases, coordinates domain logic
│   ├── usecase/         # Use case interfaces (implemented by services)
│   ├── dto/             # Commands, queries, results
│   └── mapper/          # Domain ↔ DTO mapping
└── infraestructure/      # Framework & external dependencies
    ├── adapter/
    │   ├── in/rest/     # REST controllers (inbound adapters)
    │   └── out/
    │       ├── persistence/  # JPA repositories (outbound adapters)
    │       └── security/     # Security implementations
    └── config/          # Spring configuration
```

**Key Principles:**

1. **Domain Layer**: Pure business logic, no framework dependencies
   - Domain entities validate themselves (see `Customer.java:77-99`)
   - Business rules encapsulated in domain methods (e.g., `Customer.approveKyc()`)
   - Use builder pattern for entity creation with validation

2. **Ports**: Interfaces defining contracts
   - **Inbound Ports** (use cases): `application/usecase/` - what the application can do
   - **Outbound Ports**: `domain/port/out/` - what the domain needs from infrastructure

3. **Adapters**: Implementations of ports
   - **Inbound**: REST controllers in `infraestructure/adapter/in/rest/`
   - **Outbound**: Repository adapters in `infraestructure/adapter/out/persistence/`

4. **Dependency Rule**: Dependencies point inward (Infrastructure → Application → Domain)
   - Domain has zero dependencies on outer layers
   - Application depends only on domain
   - Infrastructure depends on application and domain

### Current Modules
- **auth**: JWT-based authentication, user management
- **customer**: Customer domain aggregate with KYC and risk level management
- **account**: Bank account management (structure in place)
- **transaction**: Transaction processing (structure in place)
- **audit**: Audit trail (structure in place)

### Authentication & Security
- JWT-based authentication using `java-jwt` library
- Security config: `auth/infraestructure/config/SecurityConfig.java`
- Public endpoints: `/api/v1/auth/**` (login, registration)
- All other endpoints require JWT authentication
- JWT filter: `JwtAuthenticationFilter` validates tokens on each request
- Token provider: `JwtTokenProvider` generates and validates JWT tokens
- Stateless session management (no server-side sessions)

### Mappers
- **MapStruct** for infrastructure ↔ domain mapping (JPA entities ↔ domain models)
- Manual mappers for application layer (domain ↔ DTOs)
- MapStruct mappers annotated with `@Mapper(componentModel = "spring")`
- Annotation processors configured in `pom.xml:131-143` (both MapStruct and Lombok)

### Environment Configuration
- Copy `.env.example` to `.env` for Docker Compose
- Environment variables override `application.properties` defaults
- Required variables: DB credentials, JWT secret, CORS origins
- JWT secret generation: `openssl rand -base64 32`

## Important Patterns

### Domain Entity Pattern
Domain entities like `Customer` have:
- Two constructors: one for persistence (all fields), one builder for creation (validation)
- Builder pattern with validation (`Customer.builder()...build()`)
- Business methods that enforce state transitions (e.g., `approveKyc()`)
- Enums for type-safe states (`KycStatus`, `RiskLevel`)

### Service Pattern
Application services implement use case interfaces and:
- Use `@Service` annotation
- Inject repository ports via constructor (`@RequiredArgsConstructor`)
- Use `@Transactional` for database operations
- Throw domain exceptions (e.g., `CustomerNotFoundException`)
- Map between domain models and DTOs

### Adapter Pattern
Repository adapters:
- Implement domain port interfaces
- Use `@Component` annotation
- Inject Spring Data repositories
- Use MapStruct mappers to convert JPA entities ↔ domain models
- Never expose JPA entities outside infrastructure layer
