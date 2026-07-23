package com.sigorta.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.DefaultCorsProcessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void corsConfigurationAllowsFrontendPreflightRequest() throws Exception {
        SecurityConfig securityConfig = new SecurityConfig(null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        request.addHeader(HttpHeaders.ORIGIN, "https://ozlemyesilsigortam.info");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        request.addHeader(
                HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                "Authorization, Content-Type"
        );

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertTrue(configuration.getAllowedOriginPatterns().contains("http://localhost:3000"));
        assertTrue(configuration.getAllowedOriginPatterns().contains("https://ozlemyesilsigortam.info"));
        assertTrue(configuration.getAllowedOriginPatterns().contains("https://www.ozlemyesilsigortam.info"));
        assertEquals(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"),
                configuration.getAllowedMethods()
        );
        assertEquals(
                List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"),
                configuration.getAllowedHeaders()
        );
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean processed = new DefaultCorsProcessor().processRequest(configuration, request, response);

        assertTrue(processed);
        assertEquals(
                "https://ozlemyesilsigortam.info",
                response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
        );
        assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }
}
