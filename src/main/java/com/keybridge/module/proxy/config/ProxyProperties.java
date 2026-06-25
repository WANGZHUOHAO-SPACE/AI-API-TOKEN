package com.keybridge.module.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.proxy")
public record ProxyProperties(String mode) {

    public ProxyMode resolvedMode() {
        return ProxyMode.from(mode);
    }
}
