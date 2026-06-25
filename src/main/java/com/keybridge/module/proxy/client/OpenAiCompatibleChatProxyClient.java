package com.keybridge.module.proxy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keybridge.module.proxy.config.ProxyMode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiCompatibleChatProxyClient implements ChatProxyClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleChatProxyClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public ProxyMode mode() {
        return ProxyMode.OPENAI_COMPATIBLE;
    }

    @Override
    public UpstreamChatResult chat(ProxyCallContext context) {
        try {
            Map<String, Object> body = Map.of(
                    "model", context.model(),
                    "messages", List.of(Map.of("role", "user", "content", context.prompt())),
                    "stream", false
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(chatCompletionsUrl(context.provider().getBaseUrl())))
                    .timeout(Duration.ofSeconds(timeoutSeconds(context.provider().getTimeoutSeconds())))
                    .header("Authorization", "Bearer " + context.apiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new UpstreamCallException(response.statusCode(),
                        "上游服务返回 HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode firstChoice = root.path("choices").path(0);
            String content = firstChoice.path("message").path("content").asText(null);
            if (content == null) {
                throw new UpstreamCallException(502, "上游响应缺少 choices[0].message.content");
            }
            JsonNode usage = root.path("usage");
            return new UpstreamChatResult(
                    content,
                    firstChoice.path("finish_reason").asText("stop"),
                    usage.path("prompt_tokens").asInt(0),
                    usage.path("completion_tokens").asInt(0)
            );
        } catch (UpstreamCallException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new UpstreamCallException("上游调用被中断", exception);
        } catch (Exception exception) {
            throw new UpstreamCallException("无法调用 OpenAI Compatible API", exception);
        }
    }

    private String chatCompletionsUrl(String baseUrl) {
        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalized.endsWith("/chat/completions")
                ? normalized
                : normalized + "/chat/completions";
    }

    private long timeoutSeconds(Integer configuredTimeout) {
        return configuredTimeout == null || configuredTimeout < 1 ? 60 : configuredTimeout;
    }
}
