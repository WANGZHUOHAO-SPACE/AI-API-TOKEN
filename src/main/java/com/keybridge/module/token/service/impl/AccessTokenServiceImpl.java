package com.keybridge.module.token.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.common.crypto.HashUtils;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.model.service.AiModelService;
import com.keybridge.module.token.dto.CreateAccessTokenRequest;
import com.keybridge.module.token.dto.CreateAccessTokenResponse;
import com.keybridge.module.token.entity.AccessToken;
import com.keybridge.module.token.entity.TokenModelPermission;
import com.keybridge.module.token.mapper.AccessTokenMapper;
import com.keybridge.module.token.service.AccessTokenService;
import com.keybridge.module.token.service.TokenModelPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl extends ServiceImpl<AccessTokenMapper, AccessToken> implements AccessTokenService {

    private final TokenModelPermissionService permissionService;
    private final AiModelService modelService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public CreateAccessTokenResponse createToken(Long userId, CreateAccessTokenRequest request) {
        if (request.expiresAt() != null && !request.expiresAt().isAfter(LocalDateTime.now())) {
            throw new BusinessException("过期时间必须晚于当前时间");
        }
        if (request.modelIds() != null && request.modelIds().stream()
                .anyMatch(modelId -> modelId == null || modelService.getById(modelId) == null)) {
            throw new BusinessException(404, "指定的模型不存在");
        }
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String plainToken = "sk-kb-" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        AccessToken token = new AccessToken();
        token.setUserId(userId);
        token.setName(request.name());
        token.setTokenPrefix(plainToken.substring(0, Math.min(16, plainToken.length())));
        token.setTokenHash(HashUtils.sha256(plainToken));
        token.setStatus(1);
        token.setExpiresAt(request.expiresAt());
        token.setRequestLimit(request.requestLimit());
        token.setTokenLimit(request.tokenLimit());
        token.setUsedTokens(0L);
        save(token);

        if (request.modelIds() != null) {
            request.modelIds().stream().distinct().forEach(modelId -> {
                TokenModelPermission permission = new TokenModelPermission();
                permission.setAccessTokenId(token.getId());
                permission.setModelId(modelId);
                permissionService.save(permission);
            });
        }
        return new CreateAccessTokenResponse(token, plainToken);
    }
}
