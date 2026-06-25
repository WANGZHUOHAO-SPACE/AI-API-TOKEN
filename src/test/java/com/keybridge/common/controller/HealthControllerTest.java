package com.keybridge.common.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.module.proxy.config.ProxyProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthControllerTest {

    @Test
    void reportsUpWhenDatabaseAndRedisAreReachable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        HealthController controller = new HealthController(
                jdbcTemplate, redisTemplate, new ProxyProperties("MOCK"), true);

        ApiResponse<Map<String, Object>> response = controller.health();

        assertEquals(200, response.getCode());
        assertEquals("UP", response.getData().get("status"));
        assertEquals("UP", response.getData().get("database"));
        assertEquals("UP", response.getData().get("redis"));
        assertEquals("MOCK", response.getData().get("proxyMode"));
        assertTrue((Boolean) response.getData().get("demoDataEnabled"));
    }

    @Test
    void reportsDegradedWhenDependenciesAreUnavailable() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new IllegalStateException("database unavailable"));
        when(redisTemplate.hasKey(anyString()))
                .thenThrow(new IllegalStateException("redis unavailable"));
        HealthController controller = new HealthController(
                jdbcTemplate, redisTemplate, new ProxyProperties("MOCK"), false);

        ApiResponse<Map<String, Object>> response = controller.health();

        assertEquals("DEGRADED", response.getData().get("status"));
        assertEquals("DOWN", response.getData().get("database"));
        assertEquals("DOWN", response.getData().get("redis"));
    }
}
