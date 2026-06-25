package com.keybridge.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigCorsTest {

    @Test
    void allowsLocalViteFallbackPort() {
        SecurityConfig securityConfig = new SecurityConfig(null, null, null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals("http://localhost:5174", configuration.checkOrigin("http://localhost:5174"));
        assertEquals("http://127.0.0.1:5173", configuration.checkOrigin("http://127.0.0.1:5173"));
    }
}
