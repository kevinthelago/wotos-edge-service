package com.wotos.wotosedgeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Declares the set of {@code /api/**} paths that do not require a JWT bearer
 * token. All other paths under {@code /api/} are protected by
 * {@link com.wotos.wotosedgeservice.security.JwtAuthFilter}.
 *
 * <p>Public paths:
 * <ul>
 *   <li>{@code /api/health} — liveness probe, no credentials required</li>
 *   <li>{@code /api/players/list} — unauthenticated player-search used before login</li>
 * </ul>
 *
 * <p>When edge-core upgrades to Spring Boot 3.2, this class should be replaced
 * with a {@code SecurityFilterChain} bean that configures
 * {@code oauth2ResourceServer(oauth2 -> oauth2.jwt(...))} and uses
 * {@code jwk-set-uri} pointing to the user-service JWKS endpoint
 * ({@code /.well-known/jwks.json}).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public Set<String> publicApiPaths() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                "/api/health",
                "/api/players/list"
        )));
    }
}
