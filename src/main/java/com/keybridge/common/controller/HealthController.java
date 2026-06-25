package com.keybridge.common.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.module.proxy.config.ProxyProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ProxyProperties proxyProperties;
    private final boolean demoDataEnabled;

    public HealthController(JdbcTemplate jdbcTemplate,
                            StringRedisTemplate redisTemplate,
                            ProxyProperties proxyProperties,
                            @Value("${app.demo.enabled:false}") boolean demoDataEnabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.proxyProperties = proxyProperties;
        this.demoDataEnabled = demoDataEnabled;
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        boolean databaseUp = checkDatabase();
        boolean redisUp = checkRedis();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", databaseUp && redisUp ? "UP" : "DEGRADED");
        result.put("service", "keybridge-backend");
        result.put("database", databaseUp ? "UP" : "DOWN");
        result.put("redis", redisUp ? "UP" : "DOWN");
        result.put("proxyMode", proxyProperties.resolvedMode().name());
        result.put("demoDataEnabled", demoDataEnabled);
        result.put("timestamp", OffsetDateTime.now().toString());
        return ApiResponse.success(result);
    }

    private boolean checkDatabase() {
        try {
            return Integer.valueOf(1).equals(jdbcTemplate.queryForObject("SELECT 1", Integer.class));
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisTemplate.hasKey("keybridge:health:probe");
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
