package com.sigorta.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void corsConfigurationAllowsFrontendOriginsAndApiMethods() {
        SecurityConfig securityConfig = new SecurityConfig(null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        request.addHeader(HttpHeaders.ORIGIN, "https://ozlemyesilsigortam.info");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertTrue(configuration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(configuration.getAllowedOrigins().contains("https://ozlemyesilsigortam.info"));
        assertEquals(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                configuration.getAllowedMethods()
        );
        assertEquals(List.of("*"), configuration.getAllowedHeaders());
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }
}
