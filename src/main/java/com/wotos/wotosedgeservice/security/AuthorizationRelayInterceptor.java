package com.wotos.wotosedgeservice.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign {@link RequestInterceptor} that copies the incoming {@code Authorization}
 * header to every outbound Feign request so that downstream services can verify
 * the caller's identity without requiring the caller to re-authenticate.
 *
 * <p>This interceptor is registered automatically because it is a Spring-managed
 * bean: Spring Cloud OpenFeign discovers all {@code RequestInterceptor} beans in
 * the application context and applies them to every Feign client.
 */
@Component
public class AuthorizationRelayInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;

        HttpServletRequest request = attrs.getRequest();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            template.header("Authorization", authHeader);
        }
    }
}
