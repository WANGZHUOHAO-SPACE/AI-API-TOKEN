package com.keybridge.module.proxy.client;

import lombok.Getter;

@Getter
public class UpstreamCallException extends RuntimeException {

    private final int upstreamStatus;

    public UpstreamCallException(int upstreamStatus, String message) {
        super(message);
        this.upstreamStatus = upstreamStatus;
    }

    public UpstreamCallException(String message, Throwable cause) {
        super(message, cause);
        this.upstreamStatus = 502;
    }
}
