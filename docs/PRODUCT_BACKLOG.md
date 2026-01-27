# Product Backlog - Core Banking System (Billetera Virtual)

> **Última actualización:** 2026-01-27
> **Versión:** 1.1.0
> **Estado del proyecto:** MVP Ready

---

## Resumen del Estado Actual

### Funcionalidades Implementadas

| Módulo | Funcionalidad | Endpoint | Estado |
|--------|---------------|----------|--------|
| Auth | Registro de usuarios | `POST /api/v1/auth/register` | ✅ Completo |
| Auth | Login con JWT | `POST /api/v1/auth/login` | ✅ Completo |
| Auth | Cambio de contraseña | `PUT /api/v1/auth/change-password` | ✅ Completo |
| Customer | Consultar mi perfil | `GET /api/v1/customers/me` | ✅ Completo |
| Customer | Consultar cliente (admin) | `GET /api/v1/customers/{id}` | ✅ Completo |
| Customer | Listar clientes (admin) | `GET /api/v1/customers` | ✅ Completo |
| Customer | Aprobar KYC (admin) | `PUT /api/v1/customers/{id}/kyc/approve` | ✅ Completo |
| Customer | Rechazar KYC (admin) | `PUT /api/v1/customers/{id}/kyc/reject` | ✅ Completo |
| Account | Crear cuenta | `POST /api/v1/accounts` | ✅ Completo |
| Account | Mis cuentas | `GET /api/v1/accounts/me` | ✅ Completo |
| Account | Mi cuenta por ID | `GET /api/v1/accounts/me/{id}` | ✅ Completo |
| Account | Consultar cuenta (admin) | `GET /api/v1/accounts/{id}` | ✅ Completo |
| Transaction | Depósito | `POST /api/v1/transactions/accounts/{id}/deposits` | ✅ Completo |
| Transaction | Retiro | `POST /api/v1/transactions/accounts/{id}/withdrawals` | ✅ Completo |
| Transaction | Historial de cuenta | `GET /api/v1/transactions/accounts/{id}/transactions` | ✅ Completo |
| Transfer | Transferir dinero | `POST /api/v1/transfers` | ✅ Completo |
| Transfer | Mi transferencia por ID | `GET /api/v1/transfers/me/{id}` | ✅ Completo |
| Transfer | Consultar transferencia (admin) | `GET /api/v1/transfers/{id}` | ✅ Completo |

### Arquitectura Implementada

- [x] Arquitectura Hexagonal (Ports & Adapters)
- [x] Autenticación JWT stateless
- [x] Sistema de permisos granular (authorities)
- [x] Validación de dominio en entidades
- [x] Manejo de excepciones global
- [x] Mappers (MapStruct + manuales)
- [x] Value Objects para integridad de datos
- [x] Idempotencia en transferencias
- [x] Migraciones con Flyway
- [x] Dockerización (docker-compose)
- [x] Documentación OpenAPI/Swagger en todos los endpoints
- [x] Verificación de ownership en endpoints de cliente

---

## Backlog de Funcionalidades

### Leyenda de Prioridades

| Prioridad | Significado |
|-----------|-------------|
| 🔴 P0 - Crítica | Bloqueante para MVP. Sin esto el producto no funciona. |
| 🟠 P1 - Alta | Esencial para experiencia de usuario mínima viable. |
| 🟡 P2 - Media | Importante pero puede lanzarse en iteración posterior. |
| 🟢 P3 - Baja | Nice-to-have. Mejoras y optimizaciones futuras. |

### Leyenda de Estimación

| Tamaño | Significado |
|--------|-------------|
| XS | < 2 horas |
| S | 2-4 horas |
| M | 4-8 horas (1 día) |
| L | 1-2 días |
| XL | 3-5 días |

---

## ✅ P0 - Funcionalidades Críticas (COMPLETADAS)

### ~~BACK-001: Gestión de KYC (Aprobar/Rechazar)~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Customer

**Implementación:**
- `PUT /api/v1/customers/{id}/kyc/approve` - Aprobación de KYC
- `PUT /api/v1/customers/{id}/kyc/reject` - Rechazo de KYC
- Protegido con `KYC_APPROVE` y `KYC_REJECT` authorities
- Documentación OpenAPI completa

---

### ~~BACK-002: Depósitos de Dinero~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Transaction

**Implementación:**
- `POST /api/v1/transactions/accounts/{accountId}/deposits`
- Validación de ownership y KYC aprobado
- Protegido con `TRANSACTION_DEPOSIT` authority
- Documentación OpenAPI completa

---

### ~~BACK-003: Retiros de Dinero~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Transaction

**Implementación:**
- `POST /api/v1/transactions/accounts/{accountId}/withdrawals`
- Validación de fondos suficientes, ownership y KYC
- Protegido con `TRANSACTION_WITHDRAW` authority
- Documentación OpenAPI completa

---

### ~~BACK-004: Historial de Transacciones por Cuenta~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Transaction

**Implementación:**
- `GET /api/v1/transactions/accounts/{accountId}/transactions`
- Paginación implementada
- Validación de ownership
- Protegido con `TRANSACTION_VIEW_OWN` authority
- Documentación OpenAPI completa

**Pendiente para mejora futura:**
- Filtros opcionales: `type`, `fromDate`, `toDate`, `status`

---

## 🟠 P1 - Funcionalidades de Alta Prioridad

### ~~BACK-005: Listar Cuentas del Usuario Autenticado~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Account

**Implementación:**
- `GET /api/v1/accounts/me` - Lista cuentas del usuario autenticado
- `GET /api/v1/accounts/me/{id}` - Consulta cuenta específica con verificación de ownership
- `GET /api/v1/accounts/{id}` - Solo para admin (`ACCOUNT_VIEW_ALL`)
- Protegido con `ACCOUNT_VIEW_OWN` authority
- Documentación OpenAPI completa

---

### BACK-006: Consultar Transacción Individual

**Prioridad:** 🟠 P1
**Estimación:** XS
**Módulo:** Transaction

**Descripción:**
Permitir consultar los detalles de una transacción específica por ID.

**Criterios de Aceptación:**
- [ ] Endpoint `GET /api/v1/transactions/{id}`
- [ ] Retornar detalles completos de la transacción
- [ ] Validar que el usuario tiene acceso (es dueño de la cuenta)
- [ ] Error 404 si no existe

---

### BACK-007: Actualizar Datos del Cliente

**Prioridad:** 🟠 P1
**Estimación:** S
**Módulo:** Customer

**Descripción:**
Permitir a los usuarios actualizar sus datos personales.

**Criterios de Aceptación:**
- [ ] Endpoint `PUT /api/v1/customers/{id}`
- [ ] Campos actualizables: nombre, teléfono, dirección
- [ ] NO actualizable: email, documento de identidad, KYC status
- [ ] Validar que el usuario solo puede actualizar su propio perfil
- [ ] Retornar datos actualizados

**Notas Técnicas:**
- `UpdateCustomerUseCase` ya está definida (vacía)
- Crear `UpdateCustomerCommand` con campos opcionales

---

### BACK-008: Consultar Balance de Cuenta

**Prioridad:** 🟠 P1
**Estimación:** XS
**Módulo:** Account

**Descripción:**
Endpoint dedicado para consultar solo el balance de una cuenta (lightweight).

**Criterios de Aceptación:**
- [ ] Endpoint `GET /api/v1/accounts/{id}/balance`
- [ ] Respuesta: `{ accountId, balance, currency, lastUpdated }`
- [ ] Validar que el usuario tiene acceso a la cuenta

---

### BACK-009: Transferencias por Alias

**Prioridad:** 🟠 P1
**Estimación:** S
**Módulo:** Transfer

**Descripción:**
Actualmente las transferencias requieren el `accountId`. Los usuarios deberían poder transferir usando el alias de la cuenta destino.

**Criterios de Aceptación:**
- [ ] Modificar `POST /api/v1/transfers` para aceptar `toAccountAlias` como alternativa a `toAccountId`
- [ ] Validar que el alias existe
- [ ] Resolver alias a accountId internamente
- [ ] Mantener compatibilidad con transferencias por ID

---

## 🟡 P2 - Funcionalidades de Prioridad Media

### BACK-010: Cierre de Cuenta

**Prioridad:** 🟡 P2
**Estimación:** S
**Módulo:** Account

**Descripción:**
Permitir cerrar cuentas bancarias.

**Criterios de Aceptación:**
- [ ] Endpoint `POST /api/v1/accounts/{id}/close`
- [ ] Validar balance = 0 antes de cerrar
- [ ] Actualizar `closedAt` con timestamp
- [ ] Cambiar estado a `CLOSED`
- [ ] Cuenta cerrada no puede recibir ni enviar fondos

**Notas Técnicas:**
- El campo `closedAt` ya existe en el modelo Account

---

### BACK-011: Límites de Transacción

**Prioridad:** 🟡 P2
**Estimación:** M
**Módulo:** Account / Transaction

**Descripción:**
Implementar validación de límites diarios y mensuales en transacciones.

**Criterios de Aceptación:**
- [ ] Validar `dailyLimit` en depósitos, retiros y transferencias salientes
- [ ] Validar `monthlyLimit` en las mismas operaciones
- [ ] Sumar transacciones del día/mes para validación
- [ ] Error descriptivo cuando se excede límite
- [ ] Los límites ya existen en el modelo Account

---

### BACK-012: Gestión de Nivel de Riesgo

**Prioridad:** 🟡 P2
**Estimación:** M
**Módulo:** Customer

**Descripción:**
Implementar lógica para gestionar el nivel de riesgo del cliente.

**Criterios de Aceptación:**
- [ ] Endpoint `PUT /api/v1/customers/{id}/risk-level`
- [ ] Niveles: LOW, MEDIUM, HIGH (ya definidos en enum)
- [ ] Solo ADMIN puede modificar
- [ ] El nivel de riesgo puede afectar límites de cuenta

---

### BACK-013: Notificaciones de Transacciones

**Prioridad:** 🟡 P2
**Estimación:** M
**Módulo:** Notification

**Descripción:**
Enviar notificaciones por email cuando se realizan transacciones.

**Criterios de Aceptación:**
- [ ] Email al realizar depósito
- [ ] Email al realizar retiro
- [ ] Email al recibir transferencia
- [ ] Email al enviar transferencia
- [ ] Template con detalles de la transacción

**Notas Técnicas:**
- El módulo de notificaciones ya existe con Thymeleaf
- Crear eventos de dominio para transacciones
- Listeners para enviar emails

---

### BACK-014: Búsqueda de Cuentas por Alias

**Prioridad:** 🟡 P2
**Estimación:** XS
**Módulo:** Account

**Descripción:**
Permitir buscar información pública de una cuenta por alias (para validar destinatario antes de transferir).

**Criterios de Aceptación:**
- [ ] Endpoint `GET /api/v1/accounts/search?alias={alias}`
- [ ] Retornar solo información pública: alias, nombre del titular (parcial), banco
- [ ] No exponer balance ni otros datos sensibles

---

## 🟢 P3 - Funcionalidades de Baja Prioridad (Post-MVP)

### BACK-015: Módulo de Auditoría

**Prioridad:** 🟢 P3
**Estimación:** L
**Módulo:** Audit

**Descripción:**
Implementar registro completo de auditoría para todas las operaciones.

**Criterios de Aceptación:**
- [ ] Registrar: usuario, acción, entidad, timestamp, IP, cambios
- [ ] Endpoint `GET /api/v1/audit` para consultar logs (solo ADMIN)
- [ ] Filtros por usuario, entidad, rango de fechas
- [ ] Retención configurable

---

### BACK-016: Reversión de Transacciones

**Prioridad:** 🟢 P3
**Estimación:** L
**Módulo:** Transaction

**Descripción:**
Permitir revertir transacciones (para casos de error o disputa).

**Criterios de Aceptación:**
- [ ] Endpoint `POST /api/v1/transactions/{id}/reverse`
- [ ] Solo ADMIN puede revertir
- [ ] Crear transacción tipo `REVERSAL`
- [ ] Actualizar balances correspondientes
- [ ] No se puede revertir una reversión

---

### BACK-017: Cálculo de Intereses

**Prioridad:** 🟢 P3
**Estimación:** L
**Módulo:** Transaction

**Descripción:**
Implementar cálculo y acreditación de intereses para cuentas de ahorro.

**Criterios de Aceptación:**
- [ ] Job programado para calcular intereses
- [ ] Solo aplica a cuentas tipo SAVINGS
- [ ] Crear transacción tipo `INTEREST`
- [ ] Tasa configurable por tipo de cuenta/moneda

---

### BACK-018: Reportes de Movimientos

**Prioridad:** 🟢 P3
**Estimación:** M
**Módulo:** Report

**Descripción:**
Generar reportes de movimientos en formato PDF/Excel.

**Criterios de Aceptación:**
- [ ] Endpoint `GET /api/v1/accounts/{id}/report`
- [ ] Query params: `format` (pdf/excel), `fromDate`, `toDate`
- [ ] Incluir resumen y detalle de movimientos
- [ ] Incluir balance inicial y final del período

---

### BACK-019: Multi-factor Authentication (MFA)

**Prioridad:** 🟢 P3
**Estimación:** XL
**Módulo:** Auth

**Descripción:**
Implementar autenticación de dos factores.

**Criterios de Aceptación:**
- [ ] Soporte para TOTP (Google Authenticator, etc.)
- [ ] Endpoint para habilitar/deshabilitar MFA
- [ ] Validación de código en login
- [ ] Códigos de respaldo

---

### BACK-020: Rate Limiting

**Prioridad:** 🟢 P3
**Estimación:** M
**Módulo:** Infrastructure

**Descripción:**
Implementar limitación de tasa de requests para prevenir abuso.

**Criterios de Aceptación:**
- [ ] Límite por IP para endpoints públicos
- [ ] Límite por usuario para endpoints autenticados
- [ ] Headers de rate limit en respuestas
- [ ] Respuesta 429 cuando se excede

---

### BACK-021: Migración de Eventos a Kafka

**Prioridad:** 🟢 P3
**Estimación:** XL
**Módulo:** Infrastructure

**Descripción:**
Migrar el sistema de eventos actual a Apache Kafka para mejor escalabilidad.

**Criterios de Aceptación:**
- [ ] Configurar Kafka en docker-compose
- [ ] Producers para eventos de dominio
- [ ] Consumers para procesamiento asíncrono
- [ ] Mantener compatibilidad con eventos actuales

---

### BACK-022: Tests de Integración

**Prioridad:** 🟢 P3
**Estimación:** L
**Módulo:** Testing

**Descripción:**
Implementar suite completa de tests de integración.

**Criterios de Aceptación:**
- [ ] Tests para todos los endpoints REST
- [ ] Tests para servicios de aplicación
- [ ] Testcontainers para PostgreSQL
- [ ] Cobertura mínima 80%

---

### ~~BACK-023: API Documentation (OpenAPI/Swagger)~~ ✅ COMPLETADO

**Estado:** ✅ Implementado
**Módulo:** Infrastructure

**Implementación:**
- SpringDoc OpenAPI integrado
- Swagger UI disponible
- Todos los controladores documentados con `@Tag`, `@Operation`, `@ApiResponses`
- `@SecurityRequirement` en endpoints protegidos
- `@Parameter` para documentar path variables y request bodies

---

### BACK-024: Health Checks y Métricas

**Prioridad:** 🟢 P3
**Estimación:** S
**Módulo:** Infrastructure

**Descripción:**
Implementar endpoints de health check y métricas para monitoreo en AWS.

**Criterios de Aceptación:**
- [ ] Endpoint `/actuator/health` con status de DB
- [ ] Endpoint `/actuator/metrics` con métricas de aplicación
- [ ] Métricas personalizadas: transacciones/minuto, usuarios activos
- [ ] Integración con CloudWatch (opcional)

---

## Roadmap Sugerido

### ~~Sprint 1 - MVP Core (P0)~~ ✅ COMPLETADO
- ~~BACK-001: Gestión de KYC~~ ✅
- ~~BACK-002: Depósitos~~ ✅
- ~~BACK-003: Retiros~~ ✅
- ~~BACK-004: Historial de Transacciones~~ ✅
- ~~BACK-005: Cuentas del usuario~~ ✅
- ~~BACK-023: Documentación API~~ ✅

### Sprint 2 - UX Mejorada (P1) ← **PRÓXIMO**
- BACK-006: Consultar transacción individual
- BACK-007: Actualizar datos del cliente
- BACK-008: Consultar balance (endpoint dedicado)
- BACK-009: Transferencias por alias

### Sprint 3 - Funcionalidades Complementarias (P2)
- BACK-010: Cierre de cuenta
- BACK-011: Límites de transacción
- BACK-012: Gestión de nivel de riesgo
- BACK-013: Notificaciones de transacciones
- BACK-014: Búsqueda por alias

### Sprint 4 - Preparación Producción (P3 seleccionados)
- BACK-024: Health checks y métricas
- BACK-022: Tests de integración
- BACK-020: Rate limiting

### Futuro
- BACK-015: Auditoría completa
- BACK-019: MFA
- BACK-021: Migración a Kafka
- BACK-016: Reversión de transacciones
- BACK-017: Cálculo de intereses
- BACK-018: Reportes PDF/Excel

---

## Notas de Despliegue AWS

### Servicios Recomendados

| Componente | Servicio AWS | Notas |
|------------|--------------|-------|
| Aplicación | ECS Fargate / EC2 | Containerizado con Docker |
| Base de datos | RDS PostgreSQL | Multi-AZ para producción |
| Secrets | Secrets Manager | JWT secret, DB credentials |
| Load Balancer | ALB | HTTPS termination |
| DNS | Route 53 | Dominio personalizado |
| Logs | CloudWatch Logs | Centralización de logs |
| Métricas | CloudWatch Metrics | Monitoreo |

### Pre-requisitos para Deploy

- [x] Implementar funcionalidades P0 ✅
- [ ] BACK-024: Health checks (para ALB)
- [ ] Configurar variables de entorno para producción
- [ ] Configurar CORS para dominio de frontend
- [ ] Revisar configuración de seguridad (HTTPS, headers)
- [ ] Configurar backups de base de datos

---

## Changelog

| Fecha | Versión | Cambios |
|-------|---------|---------|
| 2026-01-27 | 1.1.0 | Sprint 1 completado: P0 + BACK-005 + BACK-023. Sistema de permisos granular implementado. Verificación de ownership en todos los endpoints de cliente. |
| 2026-01-22 | 1.0.0 | Creación inicial del backlog |
