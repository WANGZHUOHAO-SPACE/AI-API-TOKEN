package com.keybridge.module.model.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.module.model.service.ExternalPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/pricing")
@RequiredArgsConstructor
public class PublicPricingController {

    private final ExternalPricingService pricingService;

    @GetMapping
    public ApiResponse<Map<String, Object>> pricing() {
        return ApiResponse.success(pricingService.getPricing());
    }
}
