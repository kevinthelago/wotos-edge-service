package com.wotos.wotosedgeservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Restricts cross-origin access to an explicit allowlist instead of the
 * previously wide-open {@code @CrossOrigin} on the controller.
 *
 * <p>Allowed origins come from the {@code wotos.cors.allowed-origins} property
 * (comma-separated) so they are managed per-environment through Spring Cloud
 * Config rather than hardcoded. When no origins are configured, no CORS mapping
 * is registered and cross-origin browser requests are denied by default.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(
            @Value("${wotos.cors.allowed-origins:http://localhost:3000}") String[] allowedOrigins
    ) {
        this.allowedOrigins = sanitizeOrigins(allowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.length == 0) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Trims entries and drops blanks so a stray comma or empty config value
     * cannot accidentally widen (or break) the allowlist.
     *
     * @param origins raw origins bound from configuration; may be {@code null}
     * @return a clean, blank-free array (never {@code null})
     */
    static String[] sanitizeOrigins(String[] origins) {
        if (origins == null) {
            return new String[0];
        }
        return Arrays.stream(origins)
                .filter(origin -> origin != null)
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
    }
}
