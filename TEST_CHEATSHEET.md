# Testing Cheat Sheet 🚀

## ⚡ Comandos Más Usados

```bash
# Desarrollo diario (rápido)
mvn clean verify

# Antes de commit (validar integración)
mvn clean verify -Pintegration-tests

# Validación completa (CI/CD)
mvn clean verify -Pall-tests

# Test específico
mvn test -Dtest=CustomerServiceTest

# Con coverage
mvn clean verify jacoco:report
```

## 🎯 Perfiles Disponibles

| Comando | Ejecuta | Tiempo |
|---------|---------|--------|
| `mvn clean verify` | Tests unitarios | ~1-2 min |
| `mvn clean verify -Pintegration-tests` | Tests integración | ~10-60 seg |
| `mvn clean verify -Pall-tests` | Todos | ~2-3 min |

## 📝 Convención de Nombres

- `*Test.java` → Test unitario (mocks)
- `*IT.java` → Test integración (TestContainers)

## ⚙️ Configuración Inicial (Una sola vez)

```bash
# IMPORTANTE: Crear este archivo para habilitar container reuse
echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties
```

## 🐛 Troubleshooting Rápido

```bash
# Ver perfiles disponibles
mvn help:all-profiles

# Ver tests de integración
find src/test -name "*IT.java"

# Ver contenedores activos
docker ps --filter "label=org.testcontainers=true"

# Limpiar contenedores
docker container prune

# Debug Maven
mvn clean verify -Punit-tests -X
```

## 📊 IntelliJ IDEA

**Run configurations disponibles:**
- Unit Tests
- Integration Tests
- All Tests

**Ubicación:** Dropdown arriba a la derecha → Seleccionar → Run ▶️

## 📚 Documentación Completa

- `TESTING_GUIDE.md` - Guía completa de uso
- `CAMBIOS_TESTS.md` - Resumen de cambios
- `src/test/README.md` - Guía técnica detallada
- `CLAUDE.md` - Referencia de arquitectura

## ✅ Verificación Rápida

```bash
# 1. Config existe?
cat ~/.testcontainers.properties

# 2. Tests unitarios funcionan?
mvn clean verify -Punit-tests

# 3. Tests integración funcionan?
mvn clean verify -Pintegration-tests

# 4. Reuse funciona? (debe ser <20 seg la 2da vez)
time mvn clean verify -Pintegration-tests
time mvn clean verify -Pintegration-tests
```

---

**TIP:** Guarda este archivo como favorito para consulta rápida.