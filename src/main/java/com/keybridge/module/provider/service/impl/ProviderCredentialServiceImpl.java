package com.keybridge.module.provider.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.common.crypto.AesGcmService;
import com.keybridge.common.crypto.ApiKeyMasker;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.provider.dto.CredentialRequest;
import com.keybridge.module.provider.dto.CredentialResponse;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.mapper.ProviderCredentialMapper;
import com.keybridge.module.provider.service.AiProviderService;
import com.keybridge.module.provider.service.ProviderCredentialService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProviderCredentialServiceImpl extends ServiceImpl<ProviderCredentialMapper, ProviderCredential> implements ProviderCredentialService {

    private final AesGcmService aesGcmService;
    private final AiProviderService providerService;
    private final SysUserService userService;

    @Override
    public IPage<CredentialResponse> listCredentials(long page, long size, Long providerId, SysUser operator) {
        validatePage(page, size);
        Page<ProviderCredential> credentialPage = lambdaQuery()
                .eq(!isAdmin(operator), ProviderCredential::getUserId, operator.getId())
                .eq(providerId != null, ProviderCredential::getProviderId, providerId)
                .orderByDesc(ProviderCredential::getCreatedAt)
                .page(new Page<>(page, size));
        return credentialPage.convert(this::toResponse);
    }

    @Override
    public CredentialResponse getCredential(Long id, SysUser operator) {
        return toResponse(requireManageable(id, operator));
    }

    @Override
    @Transactional
    public CredentialResponse createCredential(CredentialRequest request, SysUser owner) {
        if (request.apiKey() == null || request.apiKey().isBlank()) {
            throw new BusinessException("创建密钥时 apiKey 不能为空");
        }
        validateProvider(request.providerId());
        validateStatus(request.status());
        ProviderCredential credential = new ProviderCredential();
        credential.setUserId(owner.getId());
        applyRequest(credential, request);
        credential.setEncryptedKey(aesGcmService.encrypt(request.apiKey()));
        credential.setKeySuffix(suffix(request.apiKey()));
        credential.setFailureCount(0);
        save(credential);
        return toResponse(credential);
    }

    @Override
    @Transactional
    public CredentialResponse updateCredential(Long id, CredentialRequest request, SysUser operator) {
        ProviderCredential credential = requireManageable(id, operator);
        validateProvider(request.providerId());
        validateStatus(request.status());
        applyRequest(credential, request);
        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            credential.setEncryptedKey(aesGcmService.encrypt(request.apiKey()));
            credential.setKeySuffix(suffix(request.apiKey()));
        }
        updateById(credential);
        return toResponse(credential);
    }

    @Override
    @Transactional
    public void deleteCredential(Long id, SysUser operator) {
        ProviderCredential credential = requireManageable(id, operator);
        removeById(credential.getId());
    }

    @Override
    public ProviderCredential requireOwnedActiveCredential(Long id, Long userId) {
        ProviderCredential credential = getById(id);
        if (credential == null || !userId.equals(credential.getUserId())) {
            throw new BusinessException(404, "API Key 不存在");
        }
        if (credential.getStatus() == null || credential.getStatus() != 1) {
            throw new BusinessException(400, "API Key 已被禁用");
        }
        if (credential.getExpiresAt() != null && !credential.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(400, "API Key 已过期");
        }
        return credential;
    }

    private void applyRequest(ProviderCredential credential, CredentialRequest request) {
        credential.setProviderId(request.providerId());
        credential.setName(request.name());
        credential.setPriority(request.priority() == null ? 0 : request.priority());
        credential.setWeight(request.weight() == null ? 1 : request.weight());
        credential.setStatus(request.status() == null ? 1 : request.status());
        credential.setExpiresAt(request.expiresAt());
    }

    private void validateProvider(Long providerId) {
        if (providerService.getById(providerId) == null) {
            throw new BusinessException(404, "服务商不存在");
        }
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException("status 只能为 0 或 1");
        }
    }

    private void validatePage(long page, long size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException("分页参数不合法，size 最大为 100");
        }
    }

    private ProviderCredential requireManageable(Long id, SysUser operator) {
        ProviderCredential credential = getById(id);
        if (credential == null || (!isAdmin(operator) && !operator.getId().equals(credential.getUserId()))) {
            // Do not reveal whether another user's credential exists.
            throw new BusinessException(404, "API Key 不存在");
        }
        return credential;
    }

    private boolean isAdmin(SysUser user) {
        return "ADMIN".equals(user.getRole());
    }

    private CredentialResponse toResponse(ProviderCredential credential) {
        SysUser owner = userService.getById(credential.getUserId());
        return new CredentialResponse(
                credential.getId(),
                credential.getUserId(),
                owner == null ? null : owner.getUsername(),
                credential.getProviderId(),
                credential.getName(),
                ApiKeyMasker.maskSuffix(credential.getKeySuffix()),
                credential.getPriority(),
                credential.getWeight(),
                credential.getStatus(),
                credential.getFailureCount(),
                credential.getLastCheckedAt(),
                credential.getExpiresAt(),
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }

    private String suffix(String apiKey) {
        return apiKey.substring(Math.max(0, apiKey.length() - 4));
    }
}
