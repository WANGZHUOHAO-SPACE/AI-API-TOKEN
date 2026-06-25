package com.keybridge.module.provider.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.provider.dto.CredentialRequest;
import com.keybridge.module.provider.dto.CredentialResponse;
import com.keybridge.module.provider.service.ProviderCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class ProviderCredentialController {

    private final ProviderCredentialService credentialService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<IPage<CredentialResponse>> list(Authentication authentication,
                                                        @RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        @RequestParam(required = false) Long providerId) {
        return ApiResponse.success(credentialService.listCredentials(
                page, size, providerId, currentUserService.requireUser(authentication)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CredentialResponse> detail(Authentication authentication, @PathVariable Long id) {
        return ApiResponse.success(credentialService.getCredential(
                id, currentUserService.requireUser(authentication)));
    }

    @PostMapping
    public ApiResponse<CredentialResponse> create(Authentication authentication,
                                                   @Valid @RequestBody CredentialRequest request) {
        return ApiResponse.success(credentialService.createCredential(
                request, currentUserService.requireUser(authentication)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CredentialResponse> update(Authentication authentication,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody CredentialRequest request) {
        return ApiResponse.success(credentialService.updateCredential(
                id, request, currentUserService.requireUser(authentication)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        credentialService.deleteCredential(id, currentUserService.requireUser(authentication));
        return ApiResponse.success();
    }
}
