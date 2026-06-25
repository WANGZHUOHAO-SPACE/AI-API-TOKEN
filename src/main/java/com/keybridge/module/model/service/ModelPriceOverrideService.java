package com.keybridge.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.model.dto.ModelPriceRequest;
import com.keybridge.module.model.entity.ModelPriceOverride;

public interface ModelPriceOverrideService extends IService<ModelPriceOverride> {
    ModelPriceOverride saveOverride(String modelCode, ModelPriceRequest request, Long userId);

    void resetOverride(String modelCode);
}
