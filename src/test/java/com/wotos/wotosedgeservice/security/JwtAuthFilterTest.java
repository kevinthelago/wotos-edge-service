package com.wotos.wotosedgeservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wotos.wotosedgeservice.config.SecurityConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyZeroInteractions;

public class JwtAuthFilterTest {

    private JwtValidator jwtValidator;
    private JwtAuthFilter filter;
    private Set<String> publicPaths;

    @Before
    public void setUp() {
        jwtValidator = mock(JwtValidator.class);
        publicPaths = new SecurityConfig().publicApiPaths();
        filter = new JwtAuthFilter(jwtValidator, publicPaths, new ObjectMapper());
    }

    @Test
    public void allows_health_without_token() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/health");
        req.setServletPath("/api/health");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull("chain should be invoked for public path", chain.getRequest());
        verifyZeroInteractions(jwtValidator);
    }

    @Test
    public void allows_player_list_without_token() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/players/list");
        req.setServletPath("/api/players/list");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest());
        verifyZeroInteractions(jwtValidator);
    }

    @Test
    public void rejects401_when_no_authorization_header() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/stats/players/1/trend");
        req.setServletPath("/api/stats/players/1/trend");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
        assertNull("chain must not be invoked on 401", chain.getRequest());
    }

    @Test
    public void rejects401_when_not_bearer_scheme() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/stats/players/1/trend");
        req.setServletPath("/api/stats/players/1/trend");
        req.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
    }

    @Test
    public void rejects401_when_token_invalid() throws Exception {
        doThrow(new IllegalArgumentException("JWT has expired"))
                .when(jwtValidator).validate(anyString());

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/stats/players/1/trend");
        req.setServletPath("/api/stats/players/1/trend");
        req.addHeader("Authorization", "Bearer expiredtoken");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void passes_with_valid_token() throws Exception {
        when(jwtValidator.validate(anyString())).thenReturn(mock(Map.class));

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/stats/players/1/trend");
        req.setServletPath("/api/stats/players/1/trend");
        req.addHeader("Authorization", "Bearer validtoken");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull("chain should be invoked with a valid token", chain.getRequest());
    }

    @Test
    public void allows_options_preflight_without_token() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/api/stats/players/1/trend");
        req.setServletPath("/api/stats/players/1/trend");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest());
        verifyZeroInteractions(jwtValidator);
    }

    @Test
    public void response_body_contains_error_json_on_401() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/maps/players/5");
        req.setServletPath("/api/maps/players/5");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("\"error\""));
        assertTrue(res.getContentAsString().contains("Unauthorized"));
    }
}
