package com.keybridge.module.proxy.service;

import com.keybridge.common.crypto.AesGcmService;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.log.dto.InvocationLogCommand;
import com.keybridge.module.log.service.RequestLogService;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.service.AiProviderService;
import com.keybridge.module.provider.service.ProviderCredentialService;
import com.keybridge.module.proxy.client.ChatProxyClient;
import com.keybridge.module.proxy.client.ProxyCallContext;
import com.keybridge.module.proxy.client.UpstreamCallException;
import com.keybridge.module.proxy.client.UpstreamChatResult;
import com.keybridge.module.proxy.config.ProxyMode;
import com.keybridge.module.proxy.config.ProxyProperties;
import com.keybridge.module.proxy.dto.ChatProxyRequest;
import com.keybridge.module.proxy.dto.ChatProxyResponse;
import com.keybridge.module.proxy.dto.ChatUsage;
import com.keybridge.module.ratelimit.ProxyRateLimitService;
import com.keybridge.module.user.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ChatProxyService {

    private final ProxyProperties properties;
    private final AiProviderService providerService;
    private final ProviderCredentialService credentialService;
    private final RequestLogService requestLogService;
    private final ProxyRateLimitService rateLimitService;
    private final AesGcmService aesGcmService;
    private final Map<ProxyMode, ChatProxyClient> clients;

    public ChatProxyService(ProxyProperties properties,
                            AiProviderService providerService,
                            ProviderCredentialService credentialService,
                            RequestLogService requestLogService,
                            ProxyRateLimitService rateLimitService,
                            AesGcmService aesGcmService,
                            List<ChatProxyClient> clients) {
        this.properties = properties;
        this.providerService = providerService;
        this.credentialService = credentialService;
        this.requestLogService = requestLogService;
        this.rateLimitService = rateLimitService;
        this.aesGcmService = aesGcmService;
        this.clients = new EnumMap<>(ProxyMode.class);
        clients.forEach(client -> this.clients.put(client.mode(), client));
    }

    public ChatProxyResponse chat(ChatProxyRequest request, SysUser user) {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        long startedAt = System.nanoTime();
        AiProvider provider = null;
        UpstreamChatResult result = null;
        try {
            provider = requireProvider(request.provider());
            ProviderCredential credential = credentialService.requireOwnedActiveCredential(request.keyId(), user.getId());
            if (!provider.getId().equals(credential.getProviderId())) {
                throw new BusinessException(400, "API Key 与指定服务商不匹配");
            }

            rateLimitService.checkAndConsume(user.getId(), credential.getId());
            String plainApiKey = aesGcmService.decrypt(credential.getEncryptedKey());
            ProxyMode mode = properties.resolvedMode();
            ChatProxyClient client = clients.get(mode);
            if (client == null) {
                throw new BusinessException(500, "转发客户端未配置: " + mode);
            }

            result = client.chat(new ProxyCallContext(
                    requestId, provider, request.model(), request.prompt(), plainApiKey));
            long durationMs = elapsedMillis(startedAt);
            saveLog(requestId, user.getId(), provider.getId(), request.provider(), request.keyId(), request.model(), result,
                    durationMs, 200, true, null);
            return new ChatProxyResponse(
                    requestId,
                    mode.name(),
                    provider.getCode(),
                    request.model(),
                    result.content(),
                    result.finishReason(),
                    durationMs,
                    new ChatUsage(result.inputTokens(), result.outputTokens(), result.totalTokens())
            );
        } catch (BusinessException exception) {
            saveLog(requestId, user.getId(), provider == null ? null : provider.getId(), request.provider(),
                    request.keyId(), request.model(), result,
                    elapsedMillis(startedAt), exception.getCode(), false, exception.getMessage());
            throw exception;
        } catch (UpstreamCallException exception) {
            saveLog(requestId, user.getId(), provider == null ? null : provider.getId(), request.provider(),
                    request.keyId(), request.model(), result,
                    elapsedMillis(startedAt), 502, false, exception.getMessage());
            throw new BusinessException(502, "上游 AI 服务调用失败: " + exception.getMessage());
        } catch (Exception exception) {
            saveLog(requestId, user.getId(), provider == null ? null : provider.getId(), request.provider(),
                    request.keyId(), request.model(), result,
                    elapsedMillis(startedAt), 500, false, "统一转发内部错误");
            throw new BusinessException(500, "统一转发内部错误");
        }
    }

    private AiProvider requireProvider(String code) {
        AiProvider provider = providerService.findByCode(code.trim());
        if (provider == null) {
            throw new BusinessException(404, "服务商不存在");
        }
        if (provider.getStatus() == null || provider.getStatus() != 1) {
            throw new BusinessException(400, "服务商已停用");
        }
        if (!"OPENAI_COMPATIBLE".equalsIgnoreCase(provider.getProtocolType())) {
            throw new BusinessException(400, "当前仅支持 OpenAI Compatible 协议");
        }
        return provider;
    }

    private void saveLog(String requestId, Long userId, Long providerId, String providerCode,
                         Long credentialId, String modelName, UpstreamChatResult result,
                         long durationMs, int statusCode, boolean success, String errorMessage) {
        try {
            requestLogService.recordInvocation(new InvocationLogCommand(
                    requestId,
                    userId,
                    providerId,
                    normalizeDimension(providerCode, 50),
                    credentialId,
                    normalizeDimension(modelName, 150),
                    result == null ? 0 : result.inputTokens(),
                    result == null ? 0 : result.outputTokens(),
                    result == null ? 0 : result.totalTokens(),
                    BigDecimal.ZERO,
                    durationMs,
                    statusCode,
                    success,
                    truncate(errorMessage, 1000)
            ));
        } catch (Exception exception) {
            log.warn("Failed to save proxy request log, requestId={}", requestId, exception);
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String truncate(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String normalizeDimension(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }
}
