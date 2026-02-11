package com.banking.system.auth.infraestructure.config;

import com.banking.system.auth.infraestructure.adapter.out.filter.JwtAuthenticationFilter;
import com.banking.system.auth.infraestructure.adapter.out.filter.RateLimitFilter;
import com.banking.system.common.infraestructure.filter.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final CorrelationIdFilter correlationIdFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectProvider<RateLimitFilter> rateLimitFilterProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        /* These headers protect the client-side interaction and enforce secure communication.
         */
        http.headers(headers -> headers
                // CSP: Prevents XSS by only allowing resources from the same origin.
                .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'none'; frame-ancestors 'none';"))
                // HSTS: Instructs the browser to only use HTTPS for the next year.
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000))
                // Anti-Clickjacking: Disables embedding the API responses in iframes.
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                // Prevent MIME type sniffing to reduce XSS risks.
                .contentTypeOptions(Customizer.withDefaults())
                // Prevent caching of sensitive data in the browser.
                .cacheControl(Customizer.withDefaults())
        );

        HttpSecurity httpSecurity = http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    auth.requestMatchers(SecurityConstants.PUBLIC_URLS).permitAll();
                    auth.anyRequest().authenticated();
                });

        // Add rate limit filter only if enabled
        rateLimitFilterProvider.ifAvailable(rateLimitFilter -> httpSecurity.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class));

        return httpSecurity
                // CorrelationId filter FIRST - ensures all subsequent filters and logs have the correlation ID
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // For: JWT; JSON/XML; Content negotiation; AJAX requests; Request tracing
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        // Expose headers that client-side JavaScript can read from responses
        configuration.setExposedHeaders(List.of("Authorization", "X-Correlation-ID"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
