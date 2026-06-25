package com.keybridge.module.token.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.token.dto.CreateAccessTokenRequest;
import com.keybridge.module.token.dto.CreateAccessTokenResponse;
import com.keybridge.module.token.entity.AccessToken;

public interface AccessTokenService extends IService<AccessToken> {
    CreateAccessTokenResponse createToken(Long userId, CreateAccessTokenRequest request);
}
