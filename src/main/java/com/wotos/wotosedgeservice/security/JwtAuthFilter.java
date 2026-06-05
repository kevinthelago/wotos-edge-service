package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enforces JWT bearer authentication on all {@code /api/**} paths that are
 * not in the public allowlist defined by {@link com.wotos.wotosedgeservice.config.SecurityConfig}.
 *
 * <p>A missing or invalid token causes an immediate HTTP 401 response with a
 * JSON body — the filter chain is not continued. Valid tokens allow the request
 * to proceed normally.
 *
 * <p>Registered at {@code HIGHEST_PRECEDENCE + 1} so it runs after
 * {@link SecurityHeadersFilter} but before all application logic.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final Set<String> publicApiPaths;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(
            JwtValidator jwtValidator,
            Set<String> publicApiPaths,
            ObjectMapper objectMapper
    ) {
        this.jwtValidator = jwtValidator;
        this.publicApiPaths = publicApiPaths;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip: non-API paths, OPTIONS preflights, and explicitly public API paths
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || publicApiPaths.contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            rejectWith401(response, "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7).trim();
        try {
            jwtValidator.validate(token);
        } catch (IllegalArgumentException e) {
            rejectWith401(response, e.getMessage());
            return;
        }

        chain.doFilter(request, response);
    }

    private void rejectWith401(HttpServletResponse response, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, String> body = new HashMap<>();
        body.put("error", "Unauthorized");
        body.put("message", reason);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
