package com.keybridge.module.model.service;

import com.keybridge.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExternalPricingService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final RestClient restClient;
    private final String sourceUrl;
    private volatile CachedPricing cachedPricing;

    public ExternalPricingService(@Value("${app.pricing.source-url:https://jeniya.chat/api/pricing_new}") String sourceUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(4));
        requestFactory.setReadTimeout(Duration.ofSeconds(12));
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "KeyBridge-AI-Pricing-Sync/1.0")
                .build();
        this.sourceUrl = sourceUrl;
    }

    public Map<String, Object> getPricing() {
        CachedPricing current = cachedPricing;
        if (current != null && current.expiresAt().isAfter(Instant.now())) {
            return current.payload();
        }
        return refreshPricing();
    }

    private synchronized Map<String, Object> refreshPricing() {
        CachedPricing current = cachedPricing;
        if (current != null && current.expiresAt().isAfter(Instant.now())) {
            return current.payload();
        }
        try {
            Map<String, Object> response = restClient.get()
                    .uri(sourceUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (response == null || !Boolean.TRUE.equals(response.get("success"))
                    || !(response.get("data") instanceof List<?> models) || models.isEmpty()) {
                throw new IllegalStateException("Pricing source returned no model data");
            }
            Map<String, Object> payload = new LinkedHashMap<>(response);
            payload.put("source", sourceUrl);
            payload.put("syncedAt", OffsetDateTime.now().toString());
            Map<String, Object> immutablePayload = Collections.unmodifiableMap(payload);
            cachedPricing = new CachedPricing(immutablePayload, Instant.now().plus(CACHE_TTL));
            return immutablePayload;
        } catch (Exception exception) {
            log.warn("Failed to refresh external model pricing from {}", sourceUrl, exception);
            if (current != null) {
                return current.payload();
            }
            throw new BusinessException(502, "联网价格源暂时不可用，请稍后重试");
        }
    }

    private record CachedPricing(Map<String, Object> payload, Instant expiresAt) {
    }
}
