package com.wotos.wotosedgeservice.security;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Feign client that fetches the RS256 public-key set published by the
 * user-service at its standard JWKS discovery endpoint.
 *
 * <p>The {@code contextId} differentiates this bean from any other
 * {@code @FeignClient} that might target the same Eureka service name.
 */
@FeignClient(name = "wotos-user-service", contextId = "jwksClient")
public interface JwksClient {

    @GetMapping("/.well-known/jwks.json")
    JwkSet getJwks();
}
