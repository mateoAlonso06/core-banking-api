package com.banking.system.integration.examples;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * EJEMPLOS DE USO DE .with() PARA TESTS DE SEGURIDAD
 *
 * Este archivo es solo de referencia - no son tests reales.
 * Muestra diferentes formas de configurar seguridad en tests de integración.
 */
public class SecurityTestExamples {

    private MockMvc mockMvc;

    // ===================================================================
    // FORMA 1: Usuario sin autenticación (esperar 401)
    // ===================================================================
    void example1_NoAuthentication() throws Exception {
        // No usamos .with() - la request no tiene contexto de seguridad
        mockMvc.perform(get("/api/v1/customers/me"))
                .andReturn();
        // Resultado esperado: 401 UNAUTHORIZED
    }

    // ===================================================================
    // FORMA 2: Usuario autenticado pero sin autoridades (esperar 403)
    // ===================================================================
    void example2_AuthenticatedButNoAuthorities() throws Exception {
        // Usuario autenticado pero sin permisos específicos
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(UUID.randomUUID().toString())))
                .andReturn();
        // Resultado esperado: 403 FORBIDDEN (si el endpoint requiere permisos)
    }

    // ===================================================================
    // FORMA 3: Usuario con ROL (para @PreAuthorize("hasRole('...')"))
    // ===================================================================
    void example3_WithRole() throws Exception {
        // .roles() añade automáticamente el prefijo "ROLE_"
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(UUID.randomUUID().toString())
                        .roles("USER")))  // Esto crea authority "ROLE_USER"
                .andReturn();
        // Útil para: @PreAuthorize("hasRole('USER')")
    }

    // ===================================================================
    // FORMA 4: Usuario con AUTHORITY (para @PreAuthorize("hasAuthority('...')"))
    // ===================================================================
    void example4_WithAuthority() throws Exception {
        // .authorities() te da control total sobre los permisos exactos
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(UUID.randomUUID().toString())
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))))
                .andReturn();
        // Útil para: @PreAuthorize("hasAuthority('CUSTOMER_VIEW_OWN')")
        // Este es el approach que usas en tu proyecto!
    }

    // ===================================================================
    // FORMA 5: Usuario con múltiples authorities
    // ===================================================================
    void example5_MultipleAuthorities() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/me")
                .with(user(UUID.randomUUID().toString())
                        .authorities(
                                new SimpleGrantedAuthority("ACCOUNT_VIEW_OWN"),
                                new SimpleGrantedAuthority("ACCOUNT_CREATE"),
                                new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN")
                        )))
                .andReturn();
        // El usuario tiene múltiples permisos - útil para endpoints que requieren varios
    }

    // ===================================================================
    // FORMA 6: Usuario con rol Y authorities (combinado)
    // ===================================================================
    void example6_RoleAndAuthorities() throws Exception {
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(UUID.randomUUID().toString())
                        .roles("CUSTOMER")
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))))
                .andReturn();
        // Esto crea: ["ROLE_CUSTOMER", "CUSTOMER_VIEW_OWN"]
        // Útil si tus endpoints usan hasRole() Y hasAuthority()
    }

    // ===================================================================
    // FORMA 7: Reutilizar el mismo userId en múltiples llamadas
    // ===================================================================
    void example7_ReuseUserId() throws Exception {
        // Útil cuando necesitas simular el mismo usuario en varias operaciones
        UUID testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        // Primera llamada: obtener perfil
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(testUserId.toString())
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))))
                .andReturn();

        // Segunda llamada: actualizar perfil (mismo usuario)
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(testUserId.toString())
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_UPDATE"))))
                .andReturn();

        // Ambas llamadas simulan el MISMO usuario por el UUID
    }

    // ===================================================================
    // FORMA 8: RequestPostProcessor personalizado (avanzado)
    // ===================================================================
    void example8_CustomRequestPostProcessor() throws Exception {
        // Si necesitas más control, puedes crear tu propio RequestPostProcessor
        UUID testUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/customers/me")
                .with(request -> {
                    // Aquí tienes control total
                    var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            testUserId,  // 👈 UUID directamente, no String
                            null,
                            List.of(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))
                    );
                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                    return request;
                }))
                .andReturn();

        // Este approach es más verboso pero te da control absoluto
        // Útil si el helper .with(user(...)) no cubre tu caso de uso
    }

    // ===================================================================
    // COMPARACIÓN: ¿Por qué usar String vs UUID?
    // ===================================================================
    void comparison_StringVsUuid() throws Exception {
        UUID userId = UUID.randomUUID();

        // OPCIÓN A: Pasar como String (RECOMENDADO - más simple)
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(userId.toString())
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))))
                .andReturn();
        // ✅ Simple, legible, Spring convierte automáticamente

        // OPCIÓN B: Pasar como UUID directamente (más verboso)
        mockMvc.perform(get("/api/v1/customers/me")
                .with(request -> {
                    var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userId,  // UUID directamente
                            null,
                            List.of(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))
                    );
                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                    return request;
                }))
                .andReturn();
        // ❌ Más código, mismo resultado

        // CONCLUSIÓN: Usa la OPCIÓN A (String) para la mayoría de casos
    }

    // ===================================================================
    // RESUMEN DE TU PROYECTO
    // ===================================================================
    void yourProjectPattern() throws Exception {
        /*
         * En tu proyecto core-banking-system:
         *
         * 1. PRODUCCIÓN:
         *    - JwtAuthenticationFilter extrae userId del token JWT
         *    - Lo convierte de String a UUID
         *    - Lo establece como principal: UsernamePasswordAuthenticationToken(userId, ...)
         *    - Controllers usan @AuthenticationPrincipal UUID userId
         *
         * 2. TESTS:
         *    - Usas .with(user(UUID.toString()))
         *    - Spring Security Test crea Authentication simulado
         *    - Spring convierte automáticamente String → UUID
         *    - Controllers reciben el UUID correctamente
         *
         * 3. VENTAJAS:
         *    - No necesitas generar tokens JWT reales
         *    - Tests más rápidos
         *    - Te enfocas en testear autorización, no autenticación
         *    - Fácil modificar permisos por test
         */

        // TU PATRÓN ESTÁNDAR:
        mockMvc.perform(get("/api/v1/customers/me")
                .with(user(UUID.randomUUID().toString())
                        .authorities(new SimpleGrantedAuthority("CUSTOMER_VIEW_OWN"))))
                .andReturn();

        // Este patrón es perfecto para tus tests de integración ✅
    }
}
