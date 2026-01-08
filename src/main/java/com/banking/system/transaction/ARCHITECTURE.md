# Arquitectura: Límites entre Módulos Transaction y Account

## Problema Arquitectónico

Al desarrollar operaciones bancarias como **depósitos** y **retiros**, surge la pregunta: ¿dónde deben vivir estos use cases?

- Están fuertemente relacionados con **Account** (modifican el balance)
- Están fuertemente relacionados con **Transaction** (crean registros de auditoría)
- Uno no puede existir sin el otro

Este documento define los límites claros entre ambos módulos siguiendo principios de **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**.

---

## Solución: Separación de Responsabilidades

### Principio Fundamental

```
Account Module = Estado del Agregado (State Management)
Transaction Module = Orquestación de Operaciones + Auditoría (Operations + Audit Trail)
```

### Account Module

**Responsabilidad:** Mantener y proteger el estado de las cuentas bancarias

**Aggregate Root:** `Account`

**Qué hace:**
- ✅ Almacena el balance actual (`currentBalance`)
- ✅ Valida invariantes de dominio (fondos suficientes, moneda correcta, estado activo)
- ✅ Expone métodos de comando: `credit(Money)`, `debit(Money)`
- ✅ Gestiona ciclo de vida: crear, activar, suspender, cerrar
- ✅ Expone queries: `getBalance()`, `getStatus()`, `getCurrency()`

**Qué NO hace:**
- ❌ NO crea registros de transacciones
- ❌ NO orquesta transferencias entre cuentas
- ❌ NO maneja la lógica de depósitos/retiros (eso es orquestación)

### Transaction Module

**Responsabilidad:** Orquestar operaciones bancarias y mantener histórico de auditoría

**Aggregate Roots:** `Transaction`, `Transfer`

**Qué hace:**
- ✅ Coordina operaciones de depósito/retiro/transferencia
- ✅ Llama a métodos de Account (`credit()`, `debit()`)
- ✅ Crea registros históricos inmutables de cada operación
- ✅ Vincula transacciones relacionadas (transfers)
- ✅ Genera números de referencia únicos
- ✅ Maneja idempotencia

**Qué NO hace:**
- ❌ NO modifica directamente el balance de Account
- ❌ NO valida fondos suficientes (eso es responsabilidad de Account)
- ❌ NO gestiona el estado de las cuentas

---

## Diagrama de Bounded Contexts

```
┌─────────────────────────────────────────────────────────────┐
│                    TRANSACTION MODULE                        │
│  (Orquestación + Audit Trail)                               │
│                                                              │
│  Use Cases (inbound ports):                                  │
│  ├─ DepositUseCase         ← Coordina Account.credit()     │
│  ├─ WithdrawUseCase        ← Coordina Account.debit()      │
│  ├─ TransferUseCase        ← Coordina 2 accounts           │
│  ├─ FeeUseCase                                              │
│  ├─ InterestUseCase                                         │
│  └─ ReversalUseCase                                         │
│                                                              │
│  Domain Models:                                              │
│  ├─ Transaction                                              │
│  └─ Transfer (composite: debit + credit + fee)              │
│                                                              │
│  Depends on (outbound port):                                 │
│  └─ AccountRepositoryPort ──────────────┐                   │
└──────────────────────────────────────────┼──────────────────┘
                                           │
                                           │ Dependency Direction
                                           ↓
┌──────────────────────────────────────────────────────────────┐
│                     ACCOUNT MODULE                            │
│  (State Management + Invariantes)                            │
│                                                               │
│  Use Cases (inbound ports):                                   │
│  ├─ CreateAccountUseCase                                     │
│  ├─ CloseAccountUseCase                                      │
│  ├─ ActivateAccountUseCase                                   │
│  ├─ SuspendAccountUseCase                                    │
│  ├─ GetAccountUseCase                                        │
│  └─ GetAccountBalanceUseCase                                 │
│                                                               │
│  Domain: Account                                              │
│  Fields:                                                      │
│  ├─ id, accountNumber, customerId                            │
│  ├─ accountType, currency                                    │
│  ├─ currentBalance ← Almacena el balance (CRUD)             │
│  ├─ status (ACTIVE, SUSPENDED, CLOSED)                       │
│  └─ createdAt, updatedAt                                     │
│                                                               │
│  Business Methods (Commands):                                 │
│  ├─ credit(Money amount)     ← Agrega fondos + valida       │
│  ├─ debit(Money amount)      ← Retira fondos + valida       │
│  ├─ activate()                                                │
│  ├─ suspend()                                                 │
│  └─ close()                                                   │
│                                                               │
│  Business Methods (Queries):                                  │
│  ├─ getCurrentBalance()                                       │
│  ├─ isActive()                                                │
│  └─ getCurrency()                                             │
│                                                               │
│  Port (outbound):                                             │
│  └─ AccountRepositoryPort                                     │
└───────────────────────────────────────────────────────────────┘
```

---

## Flujo de Operaciones

### 1. Depósito (DepositUseCase)

```java
@Service
@RequiredArgsConstructor
public class TransactionService implements DepositUseCase {
    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository; // ← Outbound port

    @Override
    @Transactional
    public TransactionResult deposit(DepositCommand command) {
        // 1. Cargar Account (a través del puerto)
        Account account = accountRepository.findById(command.accountId())
            .orElseThrow(() -> new AccountNotFoundException(...));

        // 2. Account valida y modifica su propio estado
        Money amount = Money.of(command.amount(), command.currency());
        account.credit(amount); // ← Domain method:
                                //   - Valida currency matching
                                //   - Valida account activa
                                //   - Modifica currentBalance

        // 3. Crear registro de transacción
        Transaction transaction = Transaction.createNew(
            account.getId(),
            TransactionType.DEPOSIT,
            amount.getValue(),
            amount.getCurrency().code(),
            command.description(),
            generateReferenceNumber(),
            null
        );

        // 4. Marcar transacción como completada con balance resultante
        transaction = transaction.markCompleted(
            account.getCurrentBalance(),
            Instant.now()
        );

        // 5. Guardar ambos en la misma transacción de BD
        accountRepository.save(account);       // ← Balance actualizado
        Transaction saved = transactionRepository.save(transaction);

        return TransactionMapper.toResult(saved);
    }
}
```

**Flujo:**
1. Transaction carga Account
2. Transaction llama a `Account.credit(Money)` → Account se auto-valida y auto-modifica
3. Transaction crea el registro histórico
4. Transaction guarda ambos

### 2. Retiro (WithdrawUseCase)

Similar a depósito pero usando `Account.debit(Money)`:

```java
account.debit(amount); // ← Domain method:
                       //   - Valida currency matching
                       //   - Valida account activa
                       //   - Valida fondos suficientes
                       //   - Modifica currentBalance
```

Si no hay fondos suficientes, `Account.debit()` lanza `InsufficientFundsException`.

### 3. Transferencia (TransferUseCase)

```java
@Override
@Transactional
public TransferResult transfer(TransferCommand command) {
    // 1. Cargar ambas cuentas
    Account source = accountRepository.findById(command.sourceAccountId())...
    Account destination = accountRepository.findById(command.destinationAccountId())...

    Money amount = Money.of(command.amount(), command.currency());

    // 2. Validar invariante de dominio (currency matching)
    if (!source.getCurrency().equals(destination.getCurrency())) {
        throw new CurrencyMismatchException(...);
    }

    // 3. Debitar de origen (Account valida fondos)
    source.debit(amount);

    // 4. Crear transacción TRANSFER_OUT
    Transaction debitTx = Transaction.createNew(
        source.getId(),
        TransactionType.TRANSFER_OUT,
        ...
    );
    debitTx = debitTx.markCompleted(source.getCurrentBalance(), Instant.now());
    Transaction savedDebit = transactionRepository.save(debitTx);

    // 5. Acreditar a destino
    destination.credit(amount);

    // 6. Crear transacción TRANSFER_IN (vinculada)
    Transaction creditTx = Transaction.createNew(
        destination.getId(),
        TransactionType.TRANSFER_IN,
        ...,
        savedDebit.getId() // ← Vincula con la transacción de débito
    );
    creditTx = creditTx.markCompleted(destination.getCurrentBalance(), Instant.now());
    Transaction savedCredit = transactionRepository.save(creditTx);

    // 7. Crear Transfer compuesto
    Transfer transfer = Transfer.createNew(
        source.getId(),
        destination.getId(),
        savedDebit.getId(),
        savedCredit.getId(),
        ...,
        command.idempotencyKey()
    );

    // 8. Guardar todo
    accountRepository.save(source);
    accountRepository.save(destination);
    Transfer savedTransfer = transferRepository.save(transfer);

    return TransferMapper.toResult(savedTransfer);
}
```

---

## Diseño del Account Domain Model

### Account.java (Propuesta)

```java
package com.banking.system.account.domain.model;

import com.banking.system.common.domain.Money;
import com.banking.system.common.domain.MoneyCurrency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Account {
    private UUID id;
    private String accountNumber;     // Generado (ej: "ACC-1234567890")
    private UUID customerId;          // FK a Customer
    private AccountType accountType;  // SAVINGS, CHECKING, INVESTMENT
    private Money currentBalance;     // Balance actual almacenado
    private MoneyCurrency currency;   // Moneda de la cuenta
    private AccountStatus status;     // ACTIVE, SUSPENDED, CLOSED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Factory method (patrón de Customer)
    public static Account createNewAccount(
        UUID customerId,
        AccountType accountType,
        MoneyCurrency currency
    ) {
        validateCreation(customerId, accountType, currency);

        return new Account(
            null,  // id asignado por persistencia
            generateAccountNumber(),
            customerId,
            accountType,
            Money.zero(currency),  // Balance inicial = 0
            currency,
            AccountStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    // ==================== BUSINESS METHODS ====================

    // Command: Agregar fondos
    public void credit(Money amount) {
        validateActive();
        validateCurrency(amount);
        this.currentBalance = this.currentBalance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    // Command: Retirar fondos
    public void debit(Money amount) {
        validateActive();
        validateCurrency(amount);
        validateSufficientFunds(amount);
        this.currentBalance = this.currentBalance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    // Command: Suspender cuenta
    public void suspend() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot suspend closed account");
        }
        this.status = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    // Command: Activar cuenta
    public void activate() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot activate closed account");
        }
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // Command: Cerrar cuenta (solo si balance = 0)
    public void close() {
        if (!this.currentBalance.equals(Money.zero(currency))) {
            throw new IllegalStateException(
                "Cannot close account with non-zero balance. Current balance: "
                + currentBalance
            );
        }
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== VALIDATIONS ====================

    private void validateActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                "Account must be ACTIVE to perform this operation. Current status: "
                + this.status
            );
        }
    }

    private void validateCurrency(Money money) {
        if (!this.currency.equals(money.getCurrency())) {
            throw new IllegalArgumentException(
                "Currency mismatch. Account currency: " + this.currency.code()
                + ", Transaction currency: " + money.getCurrency().code()
            );
        }
    }

    private void validateSufficientFunds(Money amount) {
        if (this.currentBalance.subtract(amount).isNegative()) {
            throw new InsufficientFundsException(
                "Insufficient funds. Current balance: " + this.currentBalance
                + ", Requested: " + amount
            );
        }
    }

    private static void validateCreation(
        UUID customerId,
        AccountType accountType,
        MoneyCurrency currency
    ) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
    }

    private static String generateAccountNumber() {
        // Implementación: generar número único
        // Ej: "ACC-" + timestamp + random
        return "ACC-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    // ==================== ENUMS ====================

    public enum AccountType {
        SAVINGS,    // Cuenta de ahorros
        CHECKING,   // Cuenta corriente
        INVESTMENT  // Cuenta de inversión
    }

    public enum AccountStatus {
        ACTIVE,     // Puede realizar operaciones
        SUSPENDED,  // Temporalmente bloqueada
        CLOSED      // Cerrada permanentemente
    }
}
```

### Excepciones de Dominio

```java
// account/domain/exception/InsufficientFundsException.java
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// account/domain/exception/AccountNotFoundException.java
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

// account/domain/exception/CurrencyMismatchException.java
public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String message) {
        super(message);
    }
}
```

---

## Ajustes al Transaction Domain Model

### Agregar método markCompleted() a Transaction

```java
// transaction/domain/model/Transaction.java

public Transaction markCompleted(Money balanceAfter, Instant executedAt) {
    if (this.status != TransactionStatus.PENDING) {
        throw new IllegalStateException(
            "Only PENDING transactions can be marked as completed"
        );
    }

    return new Transaction(
        this.id,
        this.accountId,
        this.transactionType,
        this.amount,
        balanceAfter,           // ← Se establece aquí
        this.description,
        this.referenceNumber,
        this.relatedTransactionId,
        TransactionStatus.COMPLETED, // ← Cambia a COMPLETED
        executedAt,             // ← Se establece aquí
        this.createdAt
    );
}

public Transaction markFailed() {
    return new Transaction(
        this.id, this.accountId, this.transactionType, this.amount,
        this.balanceAfter, this.description, this.referenceNumber,
        this.relatedTransactionId,
        TransactionStatus.FAILED, // ← Cambia a FAILED
        Instant.now(),
        this.createdAt
    );
}
```

**Nota:** Como Transaction es inmutable (Lombok @AllArgsConstructor sin setters), estos métodos retornan una nueva instancia con el estado actualizado.

---

## Puertos (Ports)

### Account Module - Outbound Port

```java
// account/domain/port/out/AccountRepositoryPort.java
package com.banking.system.account.domain.port.out;

import com.banking.system.account.domain.model.Account;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Optional<Account> findById(UUID id);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByCustomerId(UUID customerId);
    Account save(Account account);
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean existsByAccountNumber(String accountNumber);
}
```

### Transaction Module - Outbound Port a Account

```java
// transaction/domain/port/out/AccountRepositoryPort.java
package com.banking.system.transaction.domain.port.out;

import com.banking.system.account.domain.model.Account;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port del Transaction Module para acceder a Account.
 * Transaction depende de Account a través de esta abstracción.
 */
public interface AccountRepositoryPort {
    Optional<Account> findById(UUID id);
    Account save(Account account);
}
```

**Importante:** Este port en Transaction Module es una interfaz DIFERENTE (mismo nombre, diferente paquete) que la del Account Module. En tiempo de ejecución, se inyecta el mismo adaptador de persistencia, pero Transaction solo conoce la abstracción.

---

## Principios Arquitectónicos Aplicados

### 1. Separation of Concerns (SoC)

| Módulo | Responsabilidad | QUÉ hace | QUÉ NO hace |
|--------|----------------|----------|-------------|
| **Account** | State Management | Almacenar balance<br/>Validar invariantes<br/>Modificar estado (credit/debit) | NO crear transacciones<br/>NO orquestar operaciones |
| **Transaction** | Operations + Audit | Coordinar operaciones<br/>Crear registros históricos<br/>Vincular transacciones | NO modificar balance directamente<br/>NO validar fondos |

### 2. Dependency Inversion Principle (DIP)

```
Transaction Module (alto nivel)
       ↓ depende de
AccountRepositoryPort (abstracción)
       ↑ implementado por
AccountRepositoryAdapter (bajo nivel)
```

Transaction **no conoce** cómo se persiste Account, solo conoce el contrato del puerto.

### 3. Command-Query Separation (CQS)

**Account** expone:
- **Commands** (modifican estado): `credit()`, `debit()`, `activate()`, `suspend()`, `close()`
- **Queries** (no modifican): `getCurrentBalance()`, `isActive()`, `getCurrency()`, `getStatus()`

### 4. Aggregate Boundaries

```
Account Aggregate
├─ Root: Account
├─ Invariantes:
│  ├─ currentBalance >= 0
│  ├─ currency es inmutable
│  ├─ status transitions válidas
│  └─ operaciones solo si ACTIVE

Transaction Aggregate
├─ Root: Transaction
├─ Invariantes:
│  ├─ amount > 0
│  ├─ referenceNumber único
│  └─ status transitions válidas

Transfer Aggregate
├─ Root: Transfer
├─ Invariantes:
│  ├─ sourceAccountId != destinationAccountId
│  ├─ debitTransactionId y creditTransactionId deben existir
│  └─ feeTransactionId requerido si feeAmount > 0
```

### 5. Domain Invariants

De `docs/domain/invariants.md`:

✅ **Una Account tiene exactamente una currency** → Validado en `Account.validateCurrency()`
✅ **Todas las transactions de una account usan la misma currency** → Validado en `Account.credit()/debit()`
✅ **Transfers solo entre accounts con la misma currency** → Validado en `TransactionService.transfer()`

---

## Migraciones de Base de Datos

### V5__create_accounts_table.sql

```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id),
    account_type VARCHAR(20) NOT NULL,
    current_balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_balance_non_negative CHECK (current_balance >= 0),
    CONSTRAINT chk_currency_valid CHECK (LENGTH(currency) = 3)
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_status ON accounts(status);

COMMENT ON TABLE accounts IS 'Bank accounts with stored balance (CRUD approach)';
COMMENT ON COLUMN accounts.current_balance IS 'Current balance, updated on each transaction';
```

### V6__create_transactions_table.sql

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL REFERENCES accounts(id),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    balance_after DECIMAL(19,2),
    description TEXT NOT NULL,
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    related_transaction_id UUID REFERENCES transactions(id),
    status VARCHAR(20) NOT NULL,
    executed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_reference ON transactions(reference_number);
CREATE INDEX idx_transactions_status ON transactions(status);

COMMENT ON TABLE transactions IS 'Immutable audit trail of all account operations';
COMMENT ON COLUMN transactions.balance_after IS 'Snapshot of account balance after this transaction';
COMMENT ON COLUMN transactions.related_transaction_id IS 'Links related transactions (e.g., transfer pairs)';

-- Transfers table
CREATE TABLE transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id UUID NOT NULL REFERENCES accounts(id),
    destination_account_id UUID NOT NULL REFERENCES accounts(id),
    debit_transaction_id UUID NOT NULL REFERENCES transactions(id),
    credit_transaction_id UUID NOT NULL REFERENCES transactions(id),
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    fee_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    fee_transaction_id UUID REFERENCES transactions(id),
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    executed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_transfer_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_fee_non_negative CHECK (fee_amount >= 0),
    CONSTRAINT chk_different_accounts CHECK (source_account_id != destination_account_id)
);

CREATE INDEX idx_transfers_source_account ON transfers(source_account_id);
CREATE INDEX idx_transfers_destination_account ON transfers(destination_account_id);
CREATE INDEX idx_transfers_idempotency ON transfers(idempotency_key);

COMMENT ON TABLE transfers IS 'Composite transfer records linking debit and credit transactions';
COMMENT ON COLUMN transfers.idempotency_key IS 'Prevents duplicate transfers with same idempotency key';
```

---

## Próximos Pasos de Implementación

### Fase 1: Account Module - Domain Layer
1. Crear `Account.java` con métodos `credit()`, `debit()`, lifecycle
2. Crear enums `AccountType`, `AccountStatus`
3. Crear excepciones de dominio: `InsufficientFundsException`, `AccountNotFoundException`

### Fase 2: Account Module - Application Layer
4. Crear use case interfaces: `CreateAccountUseCase`, `GetAccountUseCase`, etc.
5. Implementar `AccountService`
6. Crear DTOs (commands/results)
7. Crear mappers (domain ↔ DTO)

### Fase 3: Account Module - Infrastructure Layer
8. Crear `AccountJpaEntity`
9. Crear `SpringDataAccountRepository` (JPA)
10. Crear `AccountRepositoryAdapter` (implementa el port)
11. Crear MapStruct mapper (JPA entity ↔ domain)
12. Crear REST controller y DTOs

### Fase 4: Transaction Module - Ajustes
13. Agregar `AccountRepositoryPort` como outbound port
14. Agregar método `markCompleted()` a Transaction
15. Implementar `TransactionService` con lógica de orquestación
16. Crear `TransferRepositoryPort` y su adapter

### Fase 5: Base de Datos
17. Crear migración V5 para `accounts` table
18. Crear migración V6 para `transactions` y `transfers` tables

### Fase 6: Testing
19. Unit tests para `Account` domain logic (credit, debit, validaciones)
20. Unit tests para `Transaction` domain logic
21. Integration tests para `TransactionService` con TestContainers
22. E2E tests para flujos completos (deposit → withdraw → transfer)

---

## Decisiones de Diseño - Rationale

### ¿Por qué los use cases Deposit/Withdraw están en Transaction y no en Account?

**Respuesta:** Porque son **operaciones de coordinación** que requieren:
1. Modificar Account (llamar a `credit()`/`debit()`)
2. Crear registro de Transaction
3. Guardar ambos en transacción atómica

Si estuvieran en Account Module, Account necesitaría depender de Transaction para crear el registro → dependencia circular.

**Solución:** Transaction orquesta, Account ejecuta.

### ¿Por qué Account no tiene métodos deposit()/withdraw()?

**Respuesta:** Porque esos nombres implican **operaciones de negocio completas**, no solo modificación de estado.

- `deposit()` implica: validar, modificar balance, crear transacción, auditar
- `credit()` solo implica: validar y modificar balance

`credit()`/`debit()` son **primitivas de modificación de estado** que pueden ser compuestas por operaciones de más alto nivel (deposit, withdraw, transfer, fee, interest, etc.).

### ¿Por qué Transaction tiene un campo balanceAfter?

**Respuesta:** Para crear un **snapshot histórico** del balance en el momento de cada transacción.

Beneficios:
- Auditabilidad: puedes reconstruir el estado de la cuenta en cualquier punto del tiempo
- Reconciliación: detectar inconsistencias comparando balance actual vs último balanceAfter
- Debugging: ver la evolución del balance transacción por transacción

### ¿Por qué Transfer es un agregado separado de Transaction?

**Respuesta:** Porque Transfer representa una **operación de negocio compuesta** (débito + crédito + fee opcional) que requiere vincular múltiples transacciones.

- Transfer es la vista de alto nivel: "Se transfirieron $100 de A a B"
- Las Transactions son las operaciones atómicas: "A perdió $100", "B ganó $100"

---

## Referencias

### Documentación del Proyecto
- `docs/architecture/adr/ADR-001.md` - Decisión de usar Money Value Object
- `docs/domain/invariants.md` - Invariantes de dominio (currency)
- `CLAUDE.md` - Guía de arquitectura hexagonal del proyecto

### Patrones Aplicados
- **Domain-Driven Design (DDD):** Aggregates, Bounded Contexts, Domain Events
- **Hexagonal Architecture:** Ports & Adapters, Dependency Inversion
- **CQRS (parcial):** Command-Query Separation en Account
- **Value Objects:** Money, MoneyCurrency

### Módulos de Referencia
- `customer/` - Implementación de referencia con factory methods y validación
- `auth/` - Implementación de seguridad y JWT

---

## Conclusión

La separación entre Account y Transaction sigue el principio de **Single Responsibility**:

- **Account:** "Soy responsable de mi propio estado y de validar que sea consistente"
- **Transaction:** "Soy responsable de coordinar operaciones entre accounts y crear el registro histórico"

Esta arquitectura permite:
✅ **Bajo acoplamiento:** Transaction depende de Account via port
✅ **Alta cohesión:** Cada módulo tiene una responsabilidad clara
✅ **Testabilidad:** Puedes probar Account y Transaction independientemente
✅ **Escalabilidad:** Futura evolución a Event Sourcing si es necesario
✅ **Mantenibilidad:** Cambios en uno no afectan al otro

---

**Autor:** Análisis arquitectónico basado en DDD y Hexagonal Architecture
**Fecha:** 2026-01-08
**Versión:** 1.0
