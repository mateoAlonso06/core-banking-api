
● Análisis del Punto 2.1: Manejo de Errores en Producción

🚨 Problemas identificados en tu implementación

1. Exposición de información sensible (CRÍTICO)

Ubicación: GlobalExceptionHandler.java:83-88

@ExceptionHandler(Exception.class)                                                                                                                                                  
public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {                                                                                                            
log.error("Unexpected error", ex);                                                                                                                                              
return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",                                                                                                 
ex.getMessage());  // ⚠️ PROBLEMA: Expone mensaje interno                                                                                                               
}

Riesgo: En producción, esto puede revelar:                                                                                                                                          
┌──────────────────────┬──────────────────────────────────────┬─────────────────────────────────────────────────────────────────────┐                                               
│    Tipo de error     │         Información expuesta         │                               Ejemplo                               │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ SQL Exception        │ Nombres de tablas, columnas, queries │ ERROR: column "password_hash" does not exist                        │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ NullPointerException │ Rutas de clases internas             │ Cannot invoke "com.banking.system.auth.JwtProvider.generateToken()" │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ Network Exception    │ IPs internas, endpoints              │ Connection refused to 10.0.1.45:5432                                │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ File Exception       │ Rutas del servidor                   │ FileNotFound: /opt/banking-system/config/secrets.yml                │                                               
└──────────────────────┴──────────────────────────────────────┴─────────────────────────────────────────────────────────────────────┘                                               
Ejemplo real de ataque:

# Atacante envía request malicioso
POST /api/v1/transfer                                                                                                                                                               
{ "amount": "invalid_json }

# Respuesta actual (expone detalles):
{                                                                                                                                                                                   
"timestamp": "2026-02-09T...",                                                                                                                                                    
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "JSON parse error: Unexpected character ('}' (code 125)):                                                                                                              
was expecting double-quote to start field name at                                                                                                                      
[Source: (PushbackInputStream); line: 1, column: 23]"                                                                                                                  
}

# ❌ Ahora el atacante sabe:
# - Usas Jackson para parsear JSON
# - La ubicación exacta del error
# - Puede enviar payloads específicos para probar vulnerabilidades de Jackson
                                                                                                                                                                                      
---                                                                                                                                                                                 
2. Sin diferenciación por ambiente

Tu handler NO detecta si está en producción o desarrollo:

// ❌ ACTUAL: Mismo comportamiento en dev y prod                                                                                                                                    
return buildResponse(..., ex.getMessage());

// ✅ DEBERÍA SER:                                                                                                                                                                  
if (isProd()) {                                                                                                                                                                     
return buildResponse(..., "An unexpected error occurred");                                                                                                                      
} else {                                                                                                                                                                            
return buildResponse(..., ex.getMessage());  // Solo en dev                                                                                                                     
}
                                                                                                                                                                                      
---                                                                                                                                                                                 
3. Sin Request ID / Correlation ID

Problema: Cuando un usuario reporta un error, NO puedes correlacionar:
- La queja del usuario con los logs del servidor
- Requests entre microservicios (si escalas a futuro)
- El flujo completo de una transacción

Escenario actual:

Usuario: "Mi transferencia falló a las 15:32, request ID?"                                                                                                                          
Tú: "No tenemos request IDs... dame tu email, monto, cuenta destino..."                                                                                                             
[Buscas entre miles de logs manualmente]

Con Request ID:

Usuario: "Mi transferencia falló, request ID: 7f3a9b2c-..."                                                                                                                         
Tú: grep "7f3a9b2c" /var/log/banking.log                                                                                                                                            
[Encuentras el error en 2 segundos]
                                                                                                                                                                                      
---                                                                                                                                                                                 
4. Otros handlers también exponen información

GlobalExceptionHandler.java:72-75                                                                                                                                                   
@ExceptionHandler(IllegalArgumentException.class)                                                                                                                                   
public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {                                                                                     
log.warn("Invalid argument: {}", ex.getMessage());                                                                                                                              
return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());                                                                                                   
// ⚠️ Puede exponer validaciones internas                                                                                                                                       
}

Ejemplo problemático:

// En algún servicio interno:                                                                                                                                                       
if (amount.compareTo(MAX_TRANSFER_LIMIT) > 0) {                                                                                                                                     
throw new IllegalArgumentException(                                                                                                                                             
"Amount exceeds internal limit for risk_level=" + customer.getRiskLevel() +                                                                                                 
" (max=" + MAX_TRANSFER_LIMIT + ", attempted=" + amount + ")"                                                                                                               
);                                                                                                                                                                              
}

// Cliente recibe:                                                                                                                                                                  
{                                                                                                                                                                                   
"message": "Amount exceeds internal limit for risk_level=HIGH (max=100000, attempted=150000)"                                                                                     
}

// ❌ Ahora el atacante sabe:                                                                                                                                                       
// - Existe un risk_level interno                                                                                                                                                   
// - Los límites por nivel de riesgo                                                                                                                                                
// - Puede probar otros valores para mapear todos los límites
                                                                                                                                                                                      
---                                                                                                                                                                                 
✅ Lo que YA está bien                                                                                                                                                              
┌─────────────────────────────────────────────┬──────────────────────────────────────────────────────────────┐                                                                      
│                   Aspecto                   │                            Estado                            │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Logging con niveles apropiados              │ ✅ log.warn() para business, log.error() para infrastructure │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Manejo específico de excepciones de dominio │ ✅ ResourceNotFoundException, BusinessRuleException          │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ InfrastructureException ya es seguro        │ ✅ Línea 54: Usa mensaje genérico                            │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Estructura de respuesta consistente         │ ✅ buildResponse() centralizado                              │                                                                      
└─────────────────────────────────────────────┴──────────────────────────────────────────────────────────────┘
  ---                                                                                                                                                                                 
🔧 Solución completa

Paso 1: Crear filtro de Correlation ID

package com.banking.system.common.infraestructure.filter;

import jakarta.servlet.FilterChain;                                                                                                                                                 
import jakarta.servlet.ServletException;                                                                                                                                            
import jakarta.servlet.http.HttpServletRequest;                                                                                                                                     
import jakarta.servlet.http.HttpServletResponse;                                                                                                                                    
import org.slf4j.MDC;                                                                                                                                                               
import org.springframework.core.Ordered;                                                                                                                                            
import org.springframework.core.annotation.Order;                                                                                                                                   
import org.springframework.stereotype.Component;                                                                                                                                    
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;                                                                                                                                                         
import java.util.UUID;

@Component                                                                                                                                                                          
@Order(Ordered.HIGHEST_PRECEDENCE)  // Ejecutar PRIMERO                                                                                                                             
public class CorrelationIdFilter extends OncePerRequestFilter {

      private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";                                                                                                         
      private static final String CORRELATION_ID_MDC_KEY = "correlationId";                                                                                                           
                                                                                                                                                                                      
      @Override                                                                                                                                                                       
      protected void doFilterInternal(HttpServletRequest request,                                                                                                                     
                                       HttpServletResponse response,                                                                                                                  
                                       FilterChain filterChain) throws ServletException, IOException {                                                                                
                                                                                                                                                                                      
          // Obtener correlation ID del header o generar uno nuevo                                                                                                                    
          String correlationId = request.getHeader(CORRELATION_ID_HEADER);                                                                                                            
          if (correlationId == null || correlationId.isBlank()) {                                                                                                                     
              correlationId = UUID.randomUUID().toString();                                                                                                                           
          }                                                                                                                                                                           
                                                                                                                                                                                      
          // Agregar al contexto de logging (MDC)                                                                                                                                     
          MDC.put(CORRELATION_ID_MDC_KEY, correlationId);                                                                                                                             
                                                                                                                                                                                      
          // Agregar a la respuesta para que el cliente lo tenga                                                                                                                      
          response.setHeader(CORRELATION_ID_HEADER, correlationId);                                                                                                                   
                                                                                                                                                                                      
          try {                                                                                                                                                                       
              filterChain.doFilter(request, response);                                                                                                                                
          } finally {                                                                                                                                                                 
              // Limpiar MDC después del request                                                                                                                                      
              MDC.remove(CORRELATION_ID_MDC_KEY);                                                                                                                                     
          }                                                                                                                                                                           
      }                                                                                                                                                                               
}

Ventajas:
- Todo log ahora incluye correlationId automáticamente
- El cliente recibe el ID en el header de respuesta
- Puedes buscar logs con grep <correlationId>

  ---                                                                                                                                                                                 
Paso 2: Actualizar patrón de logging

src/main/resources/logback-spring.xml (crear si no existe):

  <?xml version="1.0" encoding="UTF-8"?>                                                                                                                                              
  <configuration>                                                                                                                                                                     
      <include resource="org/springframework/boot/logging/logback/defaults.xml"/>                                                                                                     

      <springProfile name="prod">                                                                                                                                                     
          <!-- Formato JSON para producción (mejor para ELK/CloudWatch) -->                                                                                                           
          <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">                                                                                                       
              <encoder class="net.logstash.logback.encoder.LogstashEncoder">                                                                                                          
                  <includeMdcKeyName>correlationId</includeMdcKeyName>                                                                                                                
              </encoder>                                                                                                                                                              
          </appender>                                                                                                                                                                 
      </springProfile>                                                                                                                                                                
                                                                                                                                                                                      
      <springProfile name="!prod">                                                                                                                                                    
          <!-- Formato legible para desarrollo -->                                                                                                                                    
          <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">                                                                                                       
              <encoder>                                                                                                                                                               
                  <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}] - %msg%n</pattern>                                                
              </encoder>                                                                                                                                                              
          </appender>                                                                                                                                                                 
      </springProfile>                                                                                                                                                                
                                                                                                                                                                                      
      <root level="INFO">                                                                                                                                                             
          <appender-ref ref="CONSOLE"/>                                                                                                                                               
      </root>                                                                                                                                                                         
  </configuration>                                                                                                                                                                    

Agregar dependencia en pom.xml:

  <dependency>                                                                                                                                                                        
      <groupId>net.logstash.logback</groupId>                                                                                                                                         
      <artifactId>logstash-logback-encoder</artifactId>                                                                                                                               
      <version>7.4</version>                                                                                                                                                          
  </dependency>                                                                                                                                                                       
                                                                                                                                                                                      
---                                                                                                                                                                                 
Paso 3: Actualizar GlobalExceptionHandler

package com.banking.system.common.infraestructure.exception;

import com.banking.system.auth.domain.exception.UserIsLockedException;                                                                                                              
import com.banking.system.common.domain.exception.*;                                                                                                                                
import lombok.RequiredArgsConstructor;                                                                                                                                              
import lombok.extern.slf4j.Slf4j;                                                                                                                                                   
import org.slf4j.MDC;                                                                                                                                                               
import org.springframework.core.env.Environment;                                                                                                                                    
import org.springframework.http.HttpStatus;                                                                                                                                         
import org.springframework.http.ResponseEntity;                                                                                                                                     
import org.springframework.web.bind.MethodArgumentNotValidException;                                                                                                                
import org.springframework.web.bind.annotation.ExceptionHandler;                                                                                                                    
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;                                                                                                                                                           
import java.util.Arrays;                                                                                                                                                            
import java.util.HashMap;                                                                                                                                                           
import java.util.Map;                                                                                                                                                               
import java.util.stream.Collectors;

@Slf4j                                                                                                                                                                              
@RestControllerAdvice                                                                                                                                                               
@RequiredArgsConstructor                                                                                                                                                            
public class GlobalExceptionHandler {

      private final Environment environment;                                                                                                                                          
                                                                                                                                                                                      
      @ExceptionHandler(ResourceNotFoundException.class)                                                                                                                              
      public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {                                                                                       
          log.warn("Resource not found: {}", ex.getMessage());                                                                                                                        
          return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());                                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(ResourceAlreadyExistsException.class)                                                                                                                         
      public ResponseEntity<Map<String, Object>> handleConflict(ResourceAlreadyExistsException ex) {                                                                                  
          log.warn("Resource conflict: {}", ex.getMessage());                                                                                                                         
          return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());                                                                                                     
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(AuthenticationException.class)                                                                                                                                
      public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {                                                                                   
          log.warn("Authentication failed: {}", ex.getMessage());                                                                                                                     
          return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());                                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(AccessDeniedException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {                                                                                       
          log.warn("Access denied: {}", ex.getMessage());                                                                                                                             
          return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());                                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(BusinessRuleException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {                                                                                       
          log.warn("Business rule violation: {}", ex.getMessage());                                                                                                                   
          return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(InfrastructureException.class)                                                                                                                                
      public ResponseEntity<Map<String, Object>> handleInfrastructure(InfrastructureException ex) {                                                                                   
          log.error("Infrastructure error: {}", ex.getMessage(), ex);                                                                                                                 
          return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",                                                                                             
                  "An unexpected error occurred. Please try again later.");                                                                                                           
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(MethodArgumentNotValidException.class)                                                                                                                        
      public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {                                                                               
          String errors = ex.getBindingResult()                                                                                                                                       
                  .getFieldErrors()                                                                                                                                                   
                  .stream()                                                                                                                                                           
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())                                                                                                  
                  .collect(Collectors.joining(", "));                                                                                                                                 
                                                                                                                                                                                      
          log.warn("Validation failed: {}", errors);                                                                                                                                  
          return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors);                                                                                                        
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(IllegalArgumentException.class)                                                                                                                               
      public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {                                                                                 
          log.warn("Invalid argument: {}", ex.getMessage());                                                                                                                          
                                                                                                                                                                                      
          // ⭐ NUEVO: Sanitizar mensaje en producción                                                                                                                                
          String message = isProduction()                                                                                                                                             
              ? "Invalid request parameters"                                                                                                                                          
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);                                                                                                       
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(IllegalStateException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {                                                                                       
          log.warn("Invalid state: {}", ex.getMessage());                                                                                                                             
                                                                                                                                                                                      
          // ⭐ NUEVO: Sanitizar mensaje en producción                                                                                                                                
          String message = isProduction()                                                                                                                                             
              ? "Operation cannot be performed in current state"                                                                                                                      
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.CONFLICT, "Conflict", message);                                                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(Exception.class)                                                                                                                                              
      public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {                                                                                                        
          log.error("Unexpected error", ex);                                                                                                                                          
                                                                                                                                                                                      
          // ⭐ NUEVO: Nunca exponer detalles en producción                                                                                                                           
          String message = isProduction()                                                                                                                                             
              ? "An unexpected error occurred. Please contact support with the correlation ID."                                                                                       
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message);                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(UserIsLockedException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleAccountLocked(UserIsLockedException ex) {                                                                                      
          log.warn("Account locked: {}", ex.getMessage());                                                                                                                            
          return buildResponse(HttpStatus.LOCKED, "Locked", ex.getMessage());                                                                                                         
      }                                                                                                                                                                               
                                                                                                                                                                                      
      private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {                                                                    
          Map<String, Object> body = new HashMap<>();                                                                                                                                 
          body.put("timestamp", Instant.now().toString());                                                                                                                            
          body.put("status", status.value());                                                                                                                                         
          body.put("error", error);                                                                                                                                                   
          body.put("message", message);                                                                                                                                               
                                                                                                                                                                                      
          // ⭐ NUEVO: Incluir correlation ID en la respuesta                                                                                                                         
          String correlationId = MDC.get("correlationId");                                                                                                                            
          if (correlationId != null) {                                                                                                                                                
              body.put("correlationId", correlationId);                                                                                                                               
          }                                                                                                                                                                           
                                                                                                                                                                                      
          return ResponseEntity.status(status).body(body);                                                                                                                            
      }                                                                                                                                                                               
                                                                                                                                                                                      
      // ⭐ NUEVO: Detectar ambiente de producción                                                                                                                                    
      private boolean isProduction() {                                                                                                                                                
          return Arrays.asList(environment.getActiveProfiles()).contains("prod");                                                                                                     
      }                                                                                                                                                                               
}
                                                                                                                                                                                      
---                                                                                                                                                                                 
📊 Comparación: Antes vs Después

Escenario: Error inesperado en producción

ANTES:                                                                                                                                                                              
{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "Connection refused: connect to database.internal:5432"                                                                                                                
}                                                                                                                                                                                   
❌ Revela infraestructura interna

DESPUÉS:                                                                                                                                                                            
{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "An unexpected error occurred. Please contact support with the correlation ID.",                                                                                       
"correlationId": "7f3a9b2c-e1d4-4b7a-9c3e-2f8a1b5d6e9f"                                                                                                                           
}                                                                                                                                                                                   
✅ Seguro + trazable
                                                                                                                                                                                      
---                                                                                                                                                                                 
En desarrollo (perfil dev)

{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "Connection refused: connect to database.internal:5432",                                                                                                               
"correlationId": "7f3a9b2c-e1d4-4b7a-9c3e-2f8a1b5d6e9f"                                                                                                                           
}                                                                                                                                                                                   
✅ Mensaje completo para debugging
                                                                                                                                                                                      
---                                                                                                                                                                                 
🎯 Checklist de implementación

- Crear CorrelationIdFilter.java
- Actualizar GlobalExceptionHandler.java con detección de ambiente
- Crear/actualizar logback-spring.xml con patrón MDC
- Agregar dependencia logstash-logback-encoder en pom.xml
- Probar en desarrollo que se vean mensajes completos
- Probar en producción (-Dspring.profiles.active=prod) que se oculten detalles
- Verificar que correlation ID aparezca en logs y respuestas
- Documentar en el runbook cómo buscar errores por correlation ID

  ---                                                                                                                                                                                 
¿Quieres que implemente estos cambios en tu código?

✻ Worked for 1m 42s

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
❯ si, implementa los cambios                                                                                                                                                   ↵ send
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
? for shortcuts                                                                                                                                                                     

● Análisis del Punto 2.1: Manejo de Errores en Producción

🚨 Problemas identificados en tu implementación

1. Exposición de información sensible (CRÍTICO)

Ubicación: GlobalExceptionHandler.java:83-88

@ExceptionHandler(Exception.class)                                                                                                                                                  
public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {                                                                                                            
log.error("Unexpected error", ex);                                                                                                                                              
return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",                                                                                                 
ex.getMessage());  // ⚠️ PROBLEMA: Expone mensaje interno                                                                                                               
}

Riesgo: En producción, esto puede revelar:                                                                                                                                          
┌──────────────────────┬──────────────────────────────────────┬─────────────────────────────────────────────────────────────────────┐                                               
│    Tipo de error     │         Información expuesta         │                               Ejemplo                               │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ SQL Exception        │ Nombres de tablas, columnas, queries │ ERROR: column "password_hash" does not exist                        │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ NullPointerException │ Rutas de clases internas             │ Cannot invoke "com.banking.system.auth.JwtProvider.generateToken()" │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ Network Exception    │ IPs internas, endpoints              │ Connection refused to 10.0.1.45:5432                                │                                               
├──────────────────────┼──────────────────────────────────────┼─────────────────────────────────────────────────────────────────────┤                                               
│ File Exception       │ Rutas del servidor                   │ FileNotFound: /opt/banking-system/config/secrets.yml                │                                               
└──────────────────────┴──────────────────────────────────────┴─────────────────────────────────────────────────────────────────────┘                                               
Ejemplo real de ataque:

# Atacante envía request malicioso
POST /api/v1/transfer                                                                                                                                                               
{ "amount": "invalid_json }

# Respuesta actual (expone detalles):
{                                                                                                                                                                                   
"timestamp": "2026-02-09T...",                                                                                                                                                    
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "JSON parse error: Unexpected character ('}' (code 125)):                                                                                                              
was expecting double-quote to start field name at                                                                                                                      
[Source: (PushbackInputStream); line: 1, column: 23]"                                                                                                                  
}

# ❌ Ahora el atacante sabe:
# - Usas Jackson para parsear JSON
# - La ubicación exacta del error
# - Puede enviar payloads específicos para probar vulnerabilidades de Jackson
                                                                                                                                                                                      
---                                                                                                                                                                                 
2. Sin diferenciación por ambiente

Tu handler NO detecta si está en producción o desarrollo:

// ❌ ACTUAL: Mismo comportamiento en dev y prod                                                                                                                                    
return buildResponse(..., ex.getMessage());

// ✅ DEBERÍA SER:                                                                                                                                                                  
if (isProd()) {                                                                                                                                                                     
return buildResponse(..., "An unexpected error occurred");                                                                                                                      
} else {                                                                                                                                                                            
return buildResponse(..., ex.getMessage());  // Solo en dev                                                                                                                     
}
                                                                                                                                                                                      
---                                                                                                                                                                                 
3. Sin Request ID / Correlation ID

Problema: Cuando un usuario reporta un error, NO puedes correlacionar:
- La queja del usuario con los logs del servidor
- Requests entre microservicios (si escalas a futuro)
- El flujo completo de una transacción

Escenario actual:

Usuario: "Mi transferencia falló a las 15:32, request ID?"                                                                                                                          
Tú: "No tenemos request IDs... dame tu email, monto, cuenta destino..."                                                                                                             
[Buscas entre miles de logs manualmente]

Con Request ID:

Usuario: "Mi transferencia falló, request ID: 7f3a9b2c-..."                                                                                                                         
Tú: grep "7f3a9b2c" /var/log/banking.log                                                                                                                                            
[Encuentras el error en 2 segundos]
                                                                                                                                                                                      
---                                                                                                                                                                                 
4. Otros handlers también exponen información

GlobalExceptionHandler.java:72-75                                                                                                                                                   
@ExceptionHandler(IllegalArgumentException.class)                                                                                                                                   
public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {                                                                                     
log.warn("Invalid argument: {}", ex.getMessage());                                                                                                                              
return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());                                                                                                   
// ⚠️ Puede exponer validaciones internas                                                                                                                                       
}

Ejemplo problemático:

// En algún servicio interno:                                                                                                                                                       
if (amount.compareTo(MAX_TRANSFER_LIMIT) > 0) {                                                                                                                                     
throw new IllegalArgumentException(                                                                                                                                             
"Amount exceeds internal limit for risk_level=" + customer.getRiskLevel() +                                                                                                 
" (max=" + MAX_TRANSFER_LIMIT + ", attempted=" + amount + ")"                                                                                                               
);                                                                                                                                                                              
}

// Cliente recibe:                                                                                                                                                                  
{                                                                                                                                                                                   
"message": "Amount exceeds internal limit for risk_level=HIGH (max=100000, attempted=150000)"                                                                                     
}

// ❌ Ahora el atacante sabe:                                                                                                                                                       
// - Existe un risk_level interno                                                                                                                                                   
// - Los límites por nivel de riesgo                                                                                                                                                
// - Puede probar otros valores para mapear todos los límites
                                                                                                                                                                                      
---                                                                                                                                                                                 
✅ Lo que YA está bien                                                                                                                                                              
┌─────────────────────────────────────────────┬──────────────────────────────────────────────────────────────┐                                                                      
│                   Aspecto                   │                            Estado                            │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Logging con niveles apropiados              │ ✅ log.warn() para business, log.error() para infrastructure │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Manejo específico de excepciones de dominio │ ✅ ResourceNotFoundException, BusinessRuleException          │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ InfrastructureException ya es seguro        │ ✅ Línea 54: Usa mensaje genérico                            │                                                                      
├─────────────────────────────────────────────┼──────────────────────────────────────────────────────────────┤                                                                      
│ Estructura de respuesta consistente         │ ✅ buildResponse() centralizado                              │                                                                      
└─────────────────────────────────────────────┴──────────────────────────────────────────────────────────────┘
  ---                                                                                                                                                                                 
🔧 Solución completa

Paso 1: Crear filtro de Correlation ID

package com.banking.system.common.infraestructure.filter;

import jakarta.servlet.FilterChain;                                                                                                                                                 
import jakarta.servlet.ServletException;                                                                                                                                            
import jakarta.servlet.http.HttpServletRequest;                                                                                                                                     
import jakarta.servlet.http.HttpServletResponse;                                                                                                                                    
import org.slf4j.MDC;                                                                                                                                                               
import org.springframework.core.Ordered;                                                                                                                                            
import org.springframework.core.annotation.Order;                                                                                                                                   
import org.springframework.stereotype.Component;                                                                                                                                    
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;                                                                                                                                                         
import java.util.UUID;

@Component                                                                                                                                                                          
@Order(Ordered.HIGHEST_PRECEDENCE)  // Ejecutar PRIMERO                                                                                                                             
public class CorrelationIdFilter extends OncePerRequestFilter {

      private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";                                                                                                         
      private static final String CORRELATION_ID_MDC_KEY = "correlationId";                                                                                                           
                                                                                                                                                                                      
      @Override                                                                                                                                                                       
      protected void doFilterInternal(HttpServletRequest request,                                                                                                                     
                                       HttpServletResponse response,                                                                                                                  
                                       FilterChain filterChain) throws ServletException, IOException {                                                                                
                                                                                                                                                                                      
          // Obtener correlation ID del header o generar uno nuevo                                                                                                                    
          String correlationId = request.getHeader(CORRELATION_ID_HEADER);                                                                                                            
          if (correlationId == null || correlationId.isBlank()) {                                                                                                                     
              correlationId = UUID.randomUUID().toString();                                                                                                                           
          }                                                                                                                                                                           
                                                                                                                                                                                      
          // Agregar al contexto de logging (MDC)                                                                                                                                     
          MDC.put(CORRELATION_ID_MDC_KEY, correlationId);                                                                                                                             
                                                                                                                                                                                      
          // Agregar a la respuesta para que el cliente lo tenga                                                                                                                      
          response.setHeader(CORRELATION_ID_HEADER, correlationId);                                                                                                                   
                                                                                                                                                                                      
          try {                                                                                                                                                                       
              filterChain.doFilter(request, response);                                                                                                                                
          } finally {                                                                                                                                                                 
              // Limpiar MDC después del request                                                                                                                                      
              MDC.remove(CORRELATION_ID_MDC_KEY);                                                                                                                                     
          }                                                                                                                                                                           
      }                                                                                                                                                                               
}

Ventajas:
- Todo log ahora incluye correlationId automáticamente
- El cliente recibe el ID en el header de respuesta
- Puedes buscar logs con grep <correlationId>

  ---                                                                                                                                                                                 
Paso 2: Actualizar patrón de logging

src/main/resources/logback-spring.xml (crear si no existe):

  <?xml version="1.0" encoding="UTF-8"?>                                                                                                                                              
  <configuration>                                                                                                                                                                     
      <include resource="org/springframework/boot/logging/logback/defaults.xml"/>                                                                                                     

      <springProfile name="prod">                                                                                                                                                     
          <!-- Formato JSON para producción (mejor para ELK/CloudWatch) -->                                                                                                           
          <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">                                                                                                       
              <encoder class="net.logstash.logback.encoder.LogstashEncoder">                                                                                                          
                  <includeMdcKeyName>correlationId</includeMdcKeyName>                                                                                                                
              </encoder>                                                                                                                                                              
          </appender>                                                                                                                                                                 
      </springProfile>                                                                                                                                                                
                                                                                                                                                                                      
      <springProfile name="!prod">                                                                                                                                                    
          <!-- Formato legible para desarrollo -->                                                                                                                                    
          <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">                                                                                                       
              <encoder>                                                                                                                                                               
                  <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}] - %msg%n</pattern>                                                
              </encoder>                                                                                                                                                              
          </appender>                                                                                                                                                                 
      </springProfile>                                                                                                                                                                
                                                                                                                                                                                      
      <root level="INFO">                                                                                                                                                             
          <appender-ref ref="CONSOLE"/>                                                                                                                                               
      </root>                                                                                                                                                                         
  </configuration>                                                                                                                                                                    

Agregar dependencia en pom.xml:

  <dependency>                                                                                                                                                                        
      <groupId>net.logstash.logback</groupId>                                                                                                                                         
      <artifactId>logstash-logback-encoder</artifactId>                                                                                                                               
      <version>7.4</version>                                                                                                                                                          
  </dependency>                                                                                                                                                                       
                                                                                                                                                                                      
---                                                                                                                                                                                 
Paso 3: Actualizar GlobalExceptionHandler

package com.banking.system.common.infraestructure.exception;

import com.banking.system.auth.domain.exception.UserIsLockedException;                                                                                                              
import com.banking.system.common.domain.exception.*;                                                                                                                                
import lombok.RequiredArgsConstructor;                                                                                                                                              
import lombok.extern.slf4j.Slf4j;                                                                                                                                                   
import org.slf4j.MDC;                                                                                                                                                               
import org.springframework.core.env.Environment;                                                                                                                                    
import org.springframework.http.HttpStatus;                                                                                                                                         
import org.springframework.http.ResponseEntity;                                                                                                                                     
import org.springframework.web.bind.MethodArgumentNotValidException;                                                                                                                
import org.springframework.web.bind.annotation.ExceptionHandler;                                                                                                                    
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;                                                                                                                                                           
import java.util.Arrays;                                                                                                                                                            
import java.util.HashMap;                                                                                                                                                           
import java.util.Map;                                                                                                                                                               
import java.util.stream.Collectors;

@Slf4j                                                                                                                                                                              
@RestControllerAdvice                                                                                                                                                               
@RequiredArgsConstructor                                                                                                                                                            
public class GlobalExceptionHandler {

      private final Environment environment;                                                                                                                                          
                                                                                                                                                                                      
      @ExceptionHandler(ResourceNotFoundException.class)                                                                                                                              
      public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {                                                                                       
          log.warn("Resource not found: {}", ex.getMessage());                                                                                                                        
          return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());                                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(ResourceAlreadyExistsException.class)                                                                                                                         
      public ResponseEntity<Map<String, Object>> handleConflict(ResourceAlreadyExistsException ex) {                                                                                  
          log.warn("Resource conflict: {}", ex.getMessage());                                                                                                                         
          return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());                                                                                                     
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(AuthenticationException.class)                                                                                                                                
      public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {                                                                                   
          log.warn("Authentication failed: {}", ex.getMessage());                                                                                                                     
          return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());                                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(AccessDeniedException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {                                                                                       
          log.warn("Access denied: {}", ex.getMessage());                                                                                                                             
          return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());                                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(BusinessRuleException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {                                                                                       
          log.warn("Business rule violation: {}", ex.getMessage());                                                                                                                   
          return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(InfrastructureException.class)                                                                                                                                
      public ResponseEntity<Map<String, Object>> handleInfrastructure(InfrastructureException ex) {                                                                                   
          log.error("Infrastructure error: {}", ex.getMessage(), ex);                                                                                                                 
          return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",                                                                                             
                  "An unexpected error occurred. Please try again later.");                                                                                                           
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(MethodArgumentNotValidException.class)                                                                                                                        
      public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {                                                                               
          String errors = ex.getBindingResult()                                                                                                                                       
                  .getFieldErrors()                                                                                                                                                   
                  .stream()                                                                                                                                                           
                  .map(error -> error.getField() + ": " + error.getDefaultMessage())                                                                                                  
                  .collect(Collectors.joining(", "));                                                                                                                                 
                                                                                                                                                                                      
          log.warn("Validation failed: {}", errors);                                                                                                                                  
          return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors);                                                                                                        
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(IllegalArgumentException.class)                                                                                                                               
      public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {                                                                                 
          log.warn("Invalid argument: {}", ex.getMessage());                                                                                                                          
                                                                                                                                                                                      
          // ⭐ NUEVO: Sanitizar mensaje en producción                                                                                                                                
          String message = isProduction()                                                                                                                                             
              ? "Invalid request parameters"                                                                                                                                          
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);                                                                                                       
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(IllegalStateException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {                                                                                       
          log.warn("Invalid state: {}", ex.getMessage());                                                                                                                             
                                                                                                                                                                                      
          // ⭐ NUEVO: Sanitizar mensaje en producción                                                                                                                                
          String message = isProduction()                                                                                                                                             
              ? "Operation cannot be performed in current state"                                                                                                                      
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.CONFLICT, "Conflict", message);                                                                                                             
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(Exception.class)                                                                                                                                              
      public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {                                                                                                        
          log.error("Unexpected error", ex);                                                                                                                                          
                                                                                                                                                                                      
          // ⭐ NUEVO: Nunca exponer detalles en producción                                                                                                                           
          String message = isProduction()                                                                                                                                             
              ? "An unexpected error occurred. Please contact support with the correlation ID."                                                                                       
              : ex.getMessage();                                                                                                                                                      
                                                                                                                                                                                      
          return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message);                                                                                   
      }                                                                                                                                                                               
                                                                                                                                                                                      
      @ExceptionHandler(UserIsLockedException.class)                                                                                                                                  
      public ResponseEntity<Map<String, Object>> handleAccountLocked(UserIsLockedException ex) {                                                                                      
          log.warn("Account locked: {}", ex.getMessage());                                                                                                                            
          return buildResponse(HttpStatus.LOCKED, "Locked", ex.getMessage());                                                                                                         
      }                                                                                                                                                                               
                                                                                                                                                                                      
      private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {                                                                    
          Map<String, Object> body = new HashMap<>();                                                                                                                                 
          body.put("timestamp", Instant.now().toString());                                                                                                                            
          body.put("status", status.value());                                                                                                                                         
          body.put("error", error);                                                                                                                                                   
          body.put("message", message);                                                                                                                                               
                                                                                                                                                                                      
          // ⭐ NUEVO: Incluir correlation ID en la respuesta                                                                                                                         
          String correlationId = MDC.get("correlationId");                                                                                                                            
          if (correlationId != null) {                                                                                                                                                
              body.put("correlationId", correlationId);                                                                                                                               
          }                                                                                                                                                                           
                                                                                                                                                                                      
          return ResponseEntity.status(status).body(body);                                                                                                                            
      }                                                                                                                                                                               
                                                                                                                                                                                      
      // ⭐ NUEVO: Detectar ambiente de producción                                                                                                                                    
      private boolean isProduction() {                                                                                                                                                
          return Arrays.asList(environment.getActiveProfiles()).contains("prod");                                                                                                     
      }                                                                                                                                                                               
}
                                                                                                                                                                                      
---                                                                                                                                                                                 
📊 Comparación: Antes vs Después

Escenario: Error inesperado en producción

ANTES:                                                                                                                                                                              
{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "Connection refused: connect to database.internal:5432"                                                                                                                
}                                                                                                                                                                                   
❌ Revela infraestructura interna

DESPUÉS:                                                                                                                                                                            
{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "An unexpected error occurred. Please contact support with the correlation ID.",                                                                                       
"correlationId": "7f3a9b2c-e1d4-4b7a-9c3e-2f8a1b5d6e9f"                                                                                                                           
}                                                                                                                                                                                   
✅ Seguro + trazable
                                                                                                                                                                                      
---                                                                                                                                                                                 
En desarrollo (perfil dev)

{                                                                                                                                                                                   
"timestamp": "2026-02-09T15:32:41Z",                                                                                                                                              
"status": 500,                                                                                                                                                                    
"error": "Internal Server Error",                                                                                                                                                 
"message": "Connection refused: connect to database.internal:5432",                                                                                                               
"correlationId": "7f3a9b2c-e1d4-4b7a-9c3e-2f8a1b5d6e9f"                                                                                                                           
}                                                                                                                                                                                   
✅ Mensaje completo para debugging
                                                                                                                                                                                      
---                                                                                                                                                                                 
🎯 Checklist de implementación

- Crear CorrelationIdFilter.java
- Actualizar GlobalExceptionHandler.java con detección de ambiente
- Crear/actualizar logback-spring.xml con patrón MDC
- Agregar dependencia logstash-logback-encoder en pom.xml
- Probar en desarrollo que se vean mensajes completos
- Probar en producción (-Dspring.profiles.active=prod) que se oculten detalles
- Verificar que correlation ID aparezca en logs y respuestas
- Documentar en el runbook cómo buscar errores por correlation ID

  ---                                                                                                                                                                                 
¿Quieres que implemente estos cambios en tu código?

✻ Worked for 1m 42s

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
❯ si, implementa los cambios                                                                                                                                                   ↵ send
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
? for shortcuts                                                                                                                                                                     
