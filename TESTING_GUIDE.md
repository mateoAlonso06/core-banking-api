# Guía de Testing - Configuración y Uso

## Resumen de Cambios Aplicados

### 1. Perfiles Maven Configurados

Se agregaron **3 perfiles Maven** en `pom.xml` para separar tests unitarios de integración:

| Perfil | Qué ejecuta | Velocidad | Uso típico |
|--------|-------------|-----------|------------|
| `unit-tests` | Solo tests unitarios (`*Test.java`) | Rápido (~1-2 min) | Desarrollo diario (DEFAULT) |
| `integration-tests` | Solo tests de integración (`*IT.java`) | Moderado (~10-60 seg) | Pre-commit, validación |
| `all-tests` | Ambos tipos de tests | Lento (~2-3 min) | CI/CD, release |

### 2. TestContainers Optimizado

Se optimizó la configuración de TestContainers con:

- **Singleton Pattern**: Contenedor compartido entre todas las clases de test
- **Container Reuse**: Reutilización de contenedores entre ejecuciones
- **Imagen Ligera**: Uso de `postgres:16-alpine` (80MB vs 350MB)

### 3. Archivos Modificados

```
pom.xml                                    # Perfiles Maven agregados
src/test/java/.../AbstractIntegrationTest.java  # Optimización singleton + reuse
src/test/resources/testcontainers.properties    # Configuración de reuse (NUEVO)
CLAUDE.md                                  # Documentación actualizada
src/test/README.md                         # Guía de testing detallada (NUEVO)
.run/*.run.xml                             # Configuraciones IntelliJ IDEA (NUEVO)
```

## Cómo Usar los Perfiles

### Comandos Básicos

```bash
# ============================================
# DESARROLLO DIARIO (recomendado)
# ============================================
mvn clean verify
# o explícitamente:
mvn clean verify -Punit-tests

# Ejecuta: 519 tests unitarios
# Tiempo: ~1-2 minutos
# Skippea: Tests de integración (*IT.java)


# ============================================
# ANTES DE COMMIT
# ============================================
mvn clean verify -Pintegration-tests

# Ejecuta: Tests de integración con TestContainers
# Tiempo primera vez: ~30-60 segundos (contenedor se inicia)
# Tiempo siguiente: ~10-15 segundos (contenedor reusado)
# Skippea: Tests unitarios


# ============================================
# CI/CD o VALIDACIÓN COMPLETA
# ============================================
mvn clean verify -Pall-tests

# Ejecuta: TODOS los tests (unitarios + integración)
# Tiempo: ~2-3 minutos total
```

### Ejecutar Tests Específicos

```bash
# Test unitario específico
mvn test -Dtest=CustomerServiceTest

# Método específico
mvn test -Dtest=CustomerServiceTest#shouldCreateCustomer

# Test de integración específico
mvn verify -Pintegration-tests -Dit.test=AuthRestControllerIT
```

## Configuración Requerida: TestContainers Reuse

Para que la reutilización de contenedores funcione, debes crear un archivo de configuración global:

### Paso 1: Crear archivo de configuración

```bash
cat > ~/.testcontainers.properties << 'EOF'
# Global TestContainers configuration
testcontainers.reuse.enable=true
checks.disable=false
EOF
```

### Paso 2: Verificar

```bash
cat ~/.testcontainers.properties
```

Deberías ver:
```
testcontainers.reuse.enable=true
checks.disable=false
```

**IMPORTANTE:** Sin este archivo, los contenedores se crearán y destruirán en cada ejecución (más lento).

## Convención de Nombres

### Tests Unitarios
- **Sufijo:** `*Test.java`
- **Ubicación:** `src/test/java/.../unit/`
- **Ejemplo:** `CustomerServiceTest.java`
- **Características:**
  - Sin dependencias externas
  - Usan mocks (@Mock, @InjectMocks)
  - Rápidos (milisegundos)
  - Validan lógica de negocio aislada

### Tests de Integración
- **Sufijo:** `*IT.java`
- **Ubicación:** `src/test/java/.../integration/`
- **Ejemplo:** `AuthRestControllerIT.java`
- **Características:**
  - Usan TestContainers (PostgreSQL real)
  - Full-stack (Controller → Service → Repository → DB)
  - Más lentos (segundos)
  - Validan integración entre capas

## IntelliJ IDEA: Configuraciones de Ejecución

Se crearon 3 configuraciones listas para usar en IntelliJ:

1. **Unit Tests** - Ejecuta solo tests unitarios
2. **Integration Tests** - Ejecuta solo tests de integración
3. **All Tests** - Ejecuta todos los tests

Para usarlas:
1. Abre IntelliJ IDEA
2. Click en el dropdown de Run (arriba a la derecha)
3. Selecciona la configuración deseada
4. Click en el botón verde ▶️

## Estructura de AbstractIntegrationTest

Todos los tests de integración heredan de esta clase:

```java
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    // Singleton: contenedor compartido
    @Container
    private static final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);  // CLAVE: permite reutilización

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### Para crear un nuevo test de integración:

```java
class MyNewServiceIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDoSomething() throws Exception {
        // El contenedor PostgreSQL ya está disponible
        // No necesitas configurar nada más
    }
}
```

## Rendimiento Esperado

### Sin Optimización (Antes)
```
Primera ejecución:    ~60-90 segundos
Segunda ejecución:    ~60-90 segundos
Cada test class:      ~10-15 segundos overhead
```

### Con Optimización (Ahora)
```
Primera ejecución:    ~30-60 segundos (cold start)
Segunda ejecución:    ~10-15 segundos (warm start)
Cada test class:      ~1-2 segundos overhead
```

**Mejora:** ~4-6x más rápido en ejecuciones subsecuentes

## Workflow Recomendado

### Desarrollo Local

```bash
# 1. Escribes código
# 2. Ejecutas tests unitarios (rápido)
mvn clean verify

# 3. Si todo pasa, haces commit
git add .
git commit -m "..."

# 4. Antes de push, validas integración
mvn clean verify -Pintegration-tests

# 5. Si pasa, haces push
git push
```

### En CI/CD

```yaml
# GitHub Actions, GitLab CI, etc.
stages:
  - unit-tests:    mvn clean verify -Punit-tests
  - integration:   mvn clean verify -Pintegration-tests
  - deploy:        ...
```

## Troubleshooting

### Problema: "Container not being reused"

**Solución:**
1. Verifica que existe `~/.testcontainers.properties`
2. Verifica que contiene `testcontainers.reuse.enable=true`
3. Verifica que Docker está corriendo: `docker ps`
4. Reinicia IntelliJ/terminal

### Problema: Tests de integración muy lentos

**Causas comunes:**
- Falta configuración de reuse en `~/.testcontainers.properties`
- Estás usando imagen full de postgres (debería ser `postgres:16-alpine`)
- No heredas de `AbstractIntegrationTest`

### Problema: "Profile not found"

**Solución:**
Verifica el nombre del perfil:
```bash
# Correcto
mvn clean verify -Punit-tests
mvn clean verify -Pintegration-tests
mvn clean verify -Pall-tests

# Incorrecto
mvn clean verify -Punit
mvn clean verify -Pintegration
```

### Problema: Tests de integración se ejecutan en perfil unit-tests

**Causa:** Convención de nombres incorrecta

**Solución:**
- Tests unitarios DEBEN terminar en `Test.java`
- Tests de integración DEBEN terminar en `IT.java`

### Ver output detallado

```bash
# Debug Maven
mvn clean verify -Punit-tests -X

# Ver qué tests se ejecutan
mvn clean verify -Punit-tests -e

# Ver logs de TestContainers
export TESTCONTAINERS_DEBUG=true
mvn clean verify -Pintegration-tests
```

## Verificación Rápida

Para verificar que todo está configurado correctamente:

```bash
# 1. Verifica perfiles en pom.xml
mvn help:all-profiles

# Deberías ver:
# - unit-tests (active)
# - integration-tests
# - all-tests

# 2. Ejecuta tests unitarios (debe ser rápido)
time mvn clean verify -Punit-tests
# Esperado: ~1-2 minutos

# 3. Ejecuta tests de integración (primera vez)
time mvn clean verify -Pintegration-tests
# Esperado: ~30-60 segundos

# 4. Ejecuta tests de integración (segunda vez - debe ser más rápido)
time mvn clean verify -Pintegration-tests
# Esperado: ~10-15 segundos (si reuse funciona)
```

## Recursos Adicionales

- **Documentación completa:** `src/test/README.md`
- **Guía de arquitectura:** `CLAUDE.md`
- **Ejemplos de tests:**
  - Unit: `src/test/java/.../unit/auth/application/service/AuthServiceTest.java`
  - Integration: `src/test/java/.../integration/auth/AuthRestControllerIT.java`

## Comandos Útiles

```bash
# Limpiar contenedores huérfanos
docker container prune

# Ver contenedores de TestContainers activos
docker ps --filter "label=org.testcontainers=true"

# Detener todos los contenedores de TestContainers
docker stop $(docker ps -q --filter "label=org.testcontainers=true")

# Limpiar completamente Docker (CUIDADO: borra todo)
docker system prune -a --volumes

# Ver logs de un test específico
mvn test -Dtest=AuthServiceTest -DfailIfNoTests=false -e

# Generar reporte de coverage
mvn clean verify -Pall-tests jacoco:report
# Ver: target/site/jacoco/index.html
```

## Próximos Pasos

1. **Crear archivo de configuración global:**
   ```bash
   echo "testcontainers.reuse.enable=true" > ~/.testcontainers.properties
   ```

2. **Verificar configuración:**
   ```bash
   mvn clean verify -Punit-tests
   ```

3. **Validar integración:**
   ```bash
   mvn clean verify -Pintegration-tests
   ```

4. **Configurar tu IDE:**
   - IntelliJ: Usar las configuraciones en `.run/`
   - VS Code: Agregar tasks en `.vscode/tasks.json`

5. **Actualizar CI/CD:**
   - Usar `mvn verify -Punit-tests` para builds rápidos
   - Usar `mvn verify -Pall-tests` para validación completa

---

**¿Preguntas?** Consulta `src/test/README.md` o revisa los ejemplos en el código.