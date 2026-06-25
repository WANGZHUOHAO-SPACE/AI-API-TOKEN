package com.keybridge.module.proxy.client;

import com.keybridge.module.proxy.config.ProxyMode;
import org.springframework.stereotype.Component;

@Component
public class MockChatProxyClient implements ChatProxyClient {

    @Override
    public ProxyMode mode() {
        return ProxyMode.MOCK;
    }

    @Override
    public UpstreamChatResult chat(ProxyCallContext context) {
        if (context.apiKey() == null || context.apiKey().isBlank()) {
            throw new UpstreamCallException(500, "Mock 调用未获得解密后的 API Key");
        }
        int inputTokens = estimateTokens(context.prompt());
        String content = "[Mock] 已通过 " + context.provider().getCode()
                + " 调用模型 " + context.model() + "。统一转发链路运行正常。";
        int outputTokens = estimateTokens(content);
        return new UpstreamChatResult(content, "stop", inputTokens, outputTokens);
    }

    private int estimateTokens(String text) {
        return Math.max(1, (text.length() + 3) / 4);
    }
}
