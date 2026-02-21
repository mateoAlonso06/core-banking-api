package com.banking.system.auth.infraestructure.adapter.out.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.banking.system.auth.domain.port.out.RolePermissionCachePort;
import com.banking.system.auth.infraestructure.adapter.out.security.JwtTokenProvider;
import com.banking.system.auth.infraestructure.config.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RolePermissionCachePort rolePermissionCache;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = getJwtFromRequest(request);

            if (token != null) {
                String userIdStr = jwtTokenProvider.getUserIdFromToken(token);
                UUID userId = UUID.fromString(userIdStr);
                String role = jwtTokenProvider.getRoleFromToken(token);

                // Load permissions dynamically from cache
                Set<String> permissions = rolePermissionCache.getPermissionsForRole(role);

                List<GrantedAuthority> authorities = new ArrayList<>();
                // Add role authority with ROLE_ prefix for hasRole() checks
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                // Add permission authorities for hasAuthority() checks
                permissions.forEach(perm ->
                        authorities.add(new SimpleGrantedAuthority(perm))
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JWTVerificationException ex) {
            logger.error("JWT verification failed: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return Arrays.stream(SecurityConstants.PUBLIC_URLS)
                .anyMatch(path -> new AntPathMatcher().match(path, request.getServletPath()));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}