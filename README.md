# Core Banking System

A backend core banking system built with **Java 21** and **Spring Boot 3.5**, following **Hexagonal Architecture** (Ports & Adapters). It provides RESTful APIs for user authentication, customer management with KYC workflows, multi-currency bank account operations, and financial transactions including deposits, withdrawals, and transfers.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Modules](#modules)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [License](#license)

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Security | Spring Security 6, JWT (java-jwt 4.5.0), BCrypt |
| Database | PostgreSQL 16 |
| Cache / Rate Limiting | Redis 7, Bucket4j, Lettuce |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct 1.6.3, Lombok |
| API Docs | SpringDoc OpenAPI 2.8.5 (Swagger UI) |
| Testing | JUnit 5, TestContainers 1.19.8, JaCoCo |
| Build | Maven |
| Containerization | Docker, Docker Compose |
| Monitoring | Spring Boot Actuator (health, metrics, prometheus) |
| Notifications | Spring Mail + Thymeleaf templates (SMTP/Gmail) |

## Architecture

The project follows **Hexagonal Architecture** with strict dependency rules — dependencies always point inward:

```
Infrastructure  →  Application  →  Domain
(adapters)         (services)      (business logic)
```

Each module is structured as:

```
<module>/
├── domain/                    # Pure business logic, no framework deps
│   ├── model/                 # Entities, value objects, enums
│   ├── port/
│   │   ├── in/               # Inbound ports (use case interfaces)
│   │   └── out/              # Outbound ports (repository interfaces)
│   ├── service/              # Domain services
│   └── exception/            # Domain-specific exceptions
├── application/               # Orchestration layer
│   ├── service/              # Use case implementations
│   ├── dto/                  # Commands, queries, responses
│   ├── mapper/               # Domain ↔ DTO mapping
│   └── event/                # Domain events
└── infraestructure/           # Framework & external dependencies
    ├── adapter/
    │   ├── in/rest/          # REST controllers
    │   └── out/persistence/  # JPA repository adapters
    └── config/               # Spring configuration
```

Key design decisions:
- **Value Objects** with self-validation (`Money`, `Email`, `PersonName`, `AccountNumber`)
- **Domain Events** for cross-module communication (`UserRegisteredEvent` → auto-creates Customer, triggers verification email)
- **Permission-based authorization** (not role-based) via `@PreAuthorize`
- **Idempotency keys** on transfers to prevent duplicate processing
- **Distributed rate limiting** via Redis + Bucket4j (token bucket algorithm) to protect API from abuse

## Modules

### Auth
User registration, login, JWT token management, email verification, password changes, and two-factor authentication (2FA) via email. Supports three roles: `CUSTOMER`, `ADMIN`, `BRANCH_MANAGER` with granular permissions.

### Customer
Customer profiles linked 1:1 to users. Manages KYC (Know Your Customer) approval workflows and risk level assessment (`LOW`, `MEDIUM`, `HIGH`). Name changes automatically reset KYC status to `PENDING`.

### Account
Bank account creation and management. Generates 22-digit account numbers and user-friendly aliases. Supports multiple account types (`SAVINGS`, `CHECKING`, `INVESTMENT`), multi-currency (`ARS`, `USD`), and tracks balance with available balance (accounting for holds). Enforces one USD account per customer.

### Transaction
Deposits, withdrawals, and transfers between accounts. Supports transaction statuses (`PENDING`, `COMPLETED`, `FAILED`, `REVERSED`), fee tracking, transfer categories, and reversal capabilities. Lookup by account alias or account number.

**Deposit & withdrawal limits by account type:**

| Type | Daily Deposit | Monthly Deposit | Daily Withdrawal | Monthly Withdrawal |
|---|---|---|---|---|
| SAVINGS | 500,000.00 | 5,000,000.00 | 200,000.00 | 2,000,000.00 |
| CHECKING | 1,000,000.00 | 10,000,000.00 | 500,000.00 | 5,000,000.00 |
| INVESTMENT | 2,000,000.00 | 20,000,000.00 | 1,000,000.00 | 10,000,000.00 |

These limits are domain constants (not configurable per account). Any operation that would exceed the daily or monthly accumulated total is rejected.

### Notification
Email notifications via SMTP (Gmail). Sends verification emails on registration and welcome emails on account creation using Thymeleaf templates.

### Audit
Audit trail infrastructure (database table in place, module scaffolded).

## API Endpoints

### Authentication — `/api/v1/auth/*`

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register a new user | Public |
| POST | `/auth/login` | Authenticate and receive JWT (or 2FA session token) | Public |
| POST | `/auth/verify-email` | Verify email with token | Public |
| POST | `/auth/resend-verification` | Resend verification email | Public |
| PUT | `/auth/change-password` | Change password | Authenticated |
| POST | `/auth/2fa/verify` | Verify 2FA code and receive JWT | Public |
| PUT | `/auth/2fa/toggle` | Enable or disable 2FA | Authenticated |
| GET | `/auth/2fa/status` | Get current 2FA status | Authenticated |

### Customers — `/api/v1/customers` (authenticated)

| Method | Path | Permission |
|---|---|---|
| GET | `/customers/me` | `CUSTOMER_VIEW_OWN` |
| PUT | `/customers/me` | `CUSTOMER_UPDATE` |
| GET | `/customers/{id}` | `CUSTOMER_VIEW` |
| GET | `/customers` | `CUSTOMER_VIEW_ALL` |
| PUT | `/customers/{id}/kyc/approve` | `KYC_APPROVE` |
| PUT | `/customers/{id}/kyc/reject` | `KYC_REJECT` |

### Accounts — `/api/v1/accounts` (authenticated)

| Method | Path | Permission |
|---|---|---|
| POST | `/accounts` | `ACCOUNT_CREATE` |
| GET | `/accounts/me` | `ACCOUNT_VIEW_OWN` |
| GET | `/accounts/me/{id}` | `ACCOUNT_VIEW_OWN` |
| GET | `/accounts/{id}` | `ACCOUNT_VIEW_ALL` |
| GET | `/accounts/me/{id}/balance` | `ACCOUNT_VIEW_OWN` |
| GET | `/accounts/search?alias=...` | `ACCOUNT_VIEW_OWN` |

### Transactions — `/api/v1/transactions` (authenticated)

| Method | Path | Permission |
|---|---|---|
| POST | `/transactions/accounts/{id}/deposits` | `TRANSACTION_DEPOSIT` |
| POST | `/transactions/accounts/{id}/withdrawals` | `TRANSACTION_WITHDRAW` |
| GET | `/transactions/accounts/{id}/transactions` | `TRANSACTION_VIEW_OWN` |
| GET | `/transactions/{id}` | `TRANSACTION_VIEW_OWN` |

### Transfers — `/api/v1/transfers` (authenticated)

| Method | Path | Permission |
|---|---|---|
| POST | `/transfers` | `TRANSACTION_TRANSFER` |
| GET | `/transfers/me/{id}` | `TRANSACTION_VIEW_OWN` |
| GET | `/transfers/{id}` | `TRANSACTION_VIEW_ALL` |

Full interactive documentation available at `/swagger-ui.html` when the application is running.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Docker & Docker Compose (recommended) **or** PostgreSQL 16 + Redis 7

### Run with Docker Compose (recommended)

```bash
# 1. Clone the repository
git clone <repository-url>
cd core-banking-system

# 2. Create environment file
cp .env.example .env
# Edit .env with your values (see Environment Variables section)

# 3. Start all services (PostgreSQL, Redis, App)
docker-compose up --build

# The API will be available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Run locally

```bash
# 1. Start PostgreSQL (port 5432) and Redis (port 6379)
# 2. Create the database
createdb core_banking_db

# 3. Set environment variables or update application.yml

# 4. Build and run
mvn clean compile
mvn spring-boot:run
```

## Environment Variables

Copy `.env.example` to `.env` and configure:

| Variable | Description | Example |
|---|---|---|
| `DB_NAME` | Database name | `core_banking_db` |
| `DB_USER` | Database user | `banking_user` |
| `DB_PASSWORD` | Database password | `your_secure_password` |
| `DB_PORT` | Database port | `5432` |
| `APP_PORT` | Application port | `8080` |
| `JWT_SECRET` | JWT signing key (min 32 chars) | `openssl rand -base64 32` |
| `JWT_EXPIRATION_MS` | Token expiry in ms | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000` |
| `SPRING_DATA_REDIS_HOST` | Redis host (`redis` in Docker, `localhost` locally) | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `SPRING_DATA_REDIS_PASSWORD` | Redis password (optional) | ` ` |
| `MAIL_USERNAME` | Gmail address for SMTP | `your@gmail.com` |
| `MAIL_PASSWORD` | Gmail app password | `your_app_password` |

## Database Migrations

Migrations are managed by Flyway and run automatically on startup. Files are located in `src/main/resources/db/migration/`:

| Migration | Description |
|---|---|
| V1 | Consolidated initial schema (users, roles, permissions, customers, accounts, transactions, transfers, audit_logs, email_verification_tokens). Includes all structural changes from previous V1-V11 migrations. |
| V2 | Seed initial data (default roles: CUSTOMER, ADMIN, BRANCH_MANAGER and their associated permissions) |

The migrations have been consolidated for production deployment. Previous incremental migrations (V1-V11) are now unified into these two scripts.

## Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
# Report at target/site/jacoco/index.html

# Run unit + integration tests
mvn verify

# Run a specific test class
mvn test -Dtest=CustomerTest

# Run a specific test method
mvn test -Dtest=CustomerTest#shouldCreateCustomer
```

- **Unit tests** (`*Test.java`): Domain models, value objects, application services
- **Integration tests** (`*IT.java`): REST controllers with real PostgreSQL via TestContainers
- Coverage reporting via JaCoCo

## Project Structure

```
core-banking-system/
├── src/
│   ├── main/
│   │   ├── java/com/banking/system/
│   │   │   ├── auth/              # Authentication & authorization
│   │   │   ├── customer/          # Customer management & KYC
│   │   │   ├── account/           # Bank account operations
│   │   │   ├── transaction/       # Deposits, withdrawals, transfers
│   │   │   ├── notification/      # Email notifications
│   │   │   ├── audit/             # Audit trail
│   │   │   └── common/            # Shared value objects & exceptions
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/      # Flyway SQL migrations (V1-V2)
│   └── test/java/                 # Unit & integration tests
├── docs/                          # ADRs, DB models, domain invariants
├── docker-compose.yml
├── Dockerfile                     # Multi-stage build (JDK 21 → JRE 21)
└── pom.xml
```