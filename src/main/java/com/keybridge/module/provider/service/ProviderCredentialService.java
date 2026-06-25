package com.keybridge.module.provider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.dto.CredentialRequest;
import com.keybridge.module.provider.dto.CredentialResponse;
import com.keybridge.module.user.entity.SysUser;

public interface ProviderCredentialService extends IService<ProviderCredential> {
    IPage<CredentialResponse> listCredentials(long page, long size, Long providerId, SysUser operator);
    CredentialResponse getCredential(Long id, SysUser operator);
    CredentialResponse createCredential(CredentialRequest request, SysUser owner);
    CredentialResponse updateCredential(Long id, CredentialRequest request, SysUser operator);
    void deleteCredential(Long id, SysUser operator);
    ProviderCredential requireOwnedActiveCredential(Long id, Long userId);
}
