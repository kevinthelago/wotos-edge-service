package com.wotos.wotosedgeservice.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Adds baseline security response headers to every gateway response so the
 * service does not depend on downstream services — or the browser's defaults —
 * for clickjacking, MIME-sniffing, referrer, and transport protections.
 *
 * <p>Registered as a {@link Component} with highest precedence so the headers
 * are present even on error responses produced later in the chain.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        response.setHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains");
        filterChain.doFilter(request, response);
    }
}
