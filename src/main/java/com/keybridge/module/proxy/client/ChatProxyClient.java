package com.keybridge.module.proxy.client;

import com.keybridge.module.proxy.config.ProxyMode;

public interface ChatProxyClient {
    ProxyMode mode();
    UpstreamChatResult chat(ProxyCallContext context);
}
