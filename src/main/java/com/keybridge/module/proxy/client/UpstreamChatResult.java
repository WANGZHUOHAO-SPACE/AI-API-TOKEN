package com.keybridge.module.proxy.client;

public record UpstreamChatResult(
        String content,
        String finishReason,
        int inputTokens,
        int outputTokens
) {

    public int totalTokens() {
        return inputTokens + outputTokens;
    }
}
