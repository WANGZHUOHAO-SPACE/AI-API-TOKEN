package com.keybridge.module.proxy.client;

import com.keybridge.module.provider.entity.AiProvider;

public record ProxyCallContext(
        String requestId,
        AiProvider provider,
        String model,
        String prompt,
        String apiKey
) {
}
