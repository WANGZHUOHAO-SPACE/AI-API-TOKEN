package com.keybridge.module.token.dto;

import com.keybridge.module.token.entity.AccessToken;

public record CreateAccessTokenResponse(AccessToken accessToken, String plainToken) {
}
