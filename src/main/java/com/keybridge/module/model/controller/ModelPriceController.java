package com.keybridge.module.model.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.security.CurrentUserService;
import com.keybridge.module.model.dto.ModelPriceRequest;
import com.keybridge.module.model.entity.ModelPriceOverride;
import com.keybridge.module.model.service.ModelPriceOverrideService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/model-prices")
@RequiredArgsConstructor
public class ModelPriceController {

    private final ModelPriceOverrideService priceService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<List<ModelPriceOverride>> list() {
        return ApiResponse.success(priceService.lambdaQuery()
                .orderByDesc(ModelPriceOverride::getUpdatedAt)
                .list());
    }

    @PutMapping("/{modelCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ModelPriceOverride> update(
            Authentication authentication,
            @PathVariable @Size(max = 150) String modelCode,
            @Valid @RequestBody ModelPriceRequest request) {
        Long userId = currentUserService.requireUser(authentication).getId();
        return ApiResponse.success(priceService.saveOverride(modelCode, request, userId));
    }

    @DeleteMapping("/{modelCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> reset(@PathVariable @Size(max = 150) String modelCode) {
        priceService.resetOverride(modelCode);
        return ApiResponse.success();
    }
}
