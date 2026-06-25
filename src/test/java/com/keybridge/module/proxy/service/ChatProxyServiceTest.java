package com.keybridge.module.proxy.service;

import com.keybridge.common.crypto.AesGcmService;
import com.keybridge.common.crypto.CryptoProperties;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.log.dto.InvocationLogCommand;
import com.keybridge.module.log.service.RequestLogService;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.service.AiProviderService;
import com.keybridge.module.provider.service.ProviderCredentialService;
import com.keybridge.module.proxy.client.MockChatProxyClient;
import com.keybridge.module.proxy.config.ProxyProperties;
import com.keybridge.module.proxy.dto.ChatProxyRequest;
import com.keybridge.module.proxy.dto.ChatProxyResponse;
import com.keybridge.module.ratelimit.ProxyRateLimitService;
import com.keybridge.module.user.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatProxyServiceTest {

    private AiProviderService providerService;
    private ProviderCredentialService credentialService;
    private RequestLogService requestLogService;
    private ProxyRateLimitService rateLimitService;
    private AesGcmService aesGcmService;
    private ChatProxyService service;

    @BeforeEach
    void setUp() {
        providerService = mock(AiProviderService.class);
        credentialService = mock(ProviderCredentialService.class);
        requestLogService = mock(RequestLogService.class);
        rateLimitService = mock(ProxyRateLimitService.class);
        aesGcmService = new AesGcmService(new CryptoProperties("proxy-test-key"));
        service = new ChatProxyService(
                new ProxyProperties("MOCK"),
                providerService,
                credentialService,
                requestLogService,
                rateLimitService,
                aesGcmService,
                List.of(new MockChatProxyClient())
        );
    }

    @Test
    void shouldDecryptOwnedKeyAndCompleteMockRequest() {
        SysUser user = user(10L);
        AiProvider provider = provider(2L, "openai");
        ProviderCredential credential = credential(10L, 2L, aesGcmService.encrypt("sk-secret-value"));
        when(providerService.findByCode("openai")).thenReturn(provider);
        when(credentialService.requireOwnedActiveCredential(5L, 10L)).thenReturn(credential);

        ChatProxyResponse response = service.chat(
                new ChatProxyRequest("openai", "gpt-demo", 5L, "你好"), user);

        assertThat(response.mode()).isEqualTo("MOCK");
        assertThat(response.content()).contains("统一转发链路运行正常");
        assertThat(response.content()).doesNotContain("sk-secret-value", "你好");

        verify(rateLimitService).checkAndConsume(10L, 5L);
        ArgumentCaptor<InvocationLogCommand> logCaptor = ArgumentCaptor.forClass(InvocationLogCommand.class);
        verify(requestLogService).recordInvocation(logCaptor.capture());
        assertThat(logCaptor.getValue().success()).isTrue();
        assertThat(logCaptor.getValue().credentialId()).isEqualTo(5L);
        assertThat(logCaptor.getValue().providerCode()).isEqualTo("openai");
        assertThat(logCaptor.getValue().modelName()).isEqualTo("gpt-demo");
        assertThat(logCaptor.getValue().errorMessage()).isNull();
    }

    @Test
    void shouldRejectKeyBoundToAnotherProvider() {
        SysUser user = user(10L);
        when(providerService.findByCode("openai")).thenReturn(provider(2L, "openai"));
        when(credentialService.requireOwnedActiveCredential(5L, 10L))
                .thenReturn(credential(10L, 99L, aesGcmService.encrypt("sk-secret-value")));

        assertThatThrownBy(() -> service.chat(
                new ChatProxyRequest("openai", "gpt-demo", 5L, "hello"), user))
                .isInstanceOf(BusinessException.class)
                .hasMessage("API Key 与指定服务商不匹配");
    }

    @Test
    void shouldLogRateLimitedRequestAsFailure() {
        SysUser user = user(10L);
        when(providerService.findByCode("openai")).thenReturn(provider(2L, "openai"));
        when(credentialService.requireOwnedActiveCredential(5L, 10L))
                .thenReturn(credential(10L, 2L, aesGcmService.encrypt("sk-secret-value")));
        doThrow(new BusinessException(429, "请求过于频繁"))
                .when(rateLimitService).checkAndConsume(10L, 5L);

        assertThatThrownBy(() -> service.chat(
                new ChatProxyRequest("openai", "gpt-demo", 5L, "hello"), user))
                .isInstanceOf(BusinessException.class)
                .hasMessage("请求过于频繁");

        ArgumentCaptor<InvocationLogCommand> logCaptor = ArgumentCaptor.forClass(InvocationLogCommand.class);
        verify(requestLogService).recordInvocation(logCaptor.capture());
        assertThat(logCaptor.getValue().statusCode()).isEqualTo(429);
        assertThat(logCaptor.getValue().success()).isFalse();
        assertThat(logCaptor.getValue().errorMessage()).isEqualTo("请求过于频繁");
    }

    private AiProvider provider(Long id, String code) {
        AiProvider provider = new AiProvider();
        provider.setId(id);
        provider.setCode(code);
        provider.setStatus(1);
        provider.setProtocolType("OPENAI_COMPATIBLE");
        provider.setBaseUrl("https://example.com/v1");
        provider.setTimeoutSeconds(30);
        return provider;
    }

    private ProviderCredential credential(Long userId, Long providerId, String encryptedKey) {
        ProviderCredential credential = new ProviderCredential();
        credential.setId(5L);
        credential.setUserId(userId);
        credential.setProviderId(providerId);
        credential.setStatus(1);
        credential.setEncryptedKey(encryptedKey);
        return credential;
    }

    private SysUser user(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername("alice");
        user.setRole("USER");
        user.setStatus(1);
        return user;
    }
}
