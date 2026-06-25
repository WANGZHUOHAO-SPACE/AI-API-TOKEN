package com.keybridge.module.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.service.AiProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class AiProviderController {

    private final AiProviderService providerService;

    @GetMapping
    public ApiResponse<Page<AiProvider>> list(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(providerService.lambdaQuery()
                .orderByDesc(AiProvider::getCreatedAt).page(new Page<>(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AiProvider> create(@Valid @RequestBody AiProvider provider) {
        provider.setId(null);
        if (provider.getProtocolType() == null) provider.setProtocolType("OPENAI_COMPATIBLE");
        if (provider.getStatus() == null) provider.setStatus(1);
        if (provider.getTimeoutSeconds() == null) provider.setTimeoutSeconds(60);
        providerService.save(provider);
        return ApiResponse.success(provider);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AiProvider> update(@PathVariable Long id, @Valid @RequestBody AiProvider provider) {
        if (providerService.getById(id) == null) throw new BusinessException(404, "服务商不存在");
        provider.setId(id);
        providerService.updateById(provider);
        return ApiResponse.success(providerService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!providerService.removeById(id)) throw new BusinessException(404, "服务商不存在");
        return ApiResponse.success();
    }
}
