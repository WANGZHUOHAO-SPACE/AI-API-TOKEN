package com.keybridge.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.model.dto.ModelPriceRequest;
import com.keybridge.module.model.entity.ModelPriceOverride;
import com.keybridge.module.model.mapper.ModelPriceOverrideMapper;
import com.keybridge.module.model.service.ModelPriceOverrideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModelPriceOverrideServiceImpl
        extends ServiceImpl<ModelPriceOverrideMapper, ModelPriceOverride>
        implements ModelPriceOverrideService {

    @Override
    @Transactional
    public ModelPriceOverride saveOverride(String modelCode, ModelPriceRequest request, Long userId) {
        ModelPriceOverride override = lambdaQuery()
                .eq(ModelPriceOverride::getModelCode, modelCode)
                .one();
        if (override == null) {
            override = new ModelPriceOverride();
            override.setModelCode(modelCode);
        }
        override.setInputPrice(trim(request.inputPrice()));
        override.setCachedPrice(trim(request.cachedPrice()));
        override.setOutputPrice(trim(request.outputPrice()));
        override.setExtraPrice(trim(request.extraPrice()));
        override.setUpdatedBy(userId);
        saveOrUpdate(override);
        return getById(override.getId());
    }

    @Override
    public void resetOverride(String modelCode) {
        lambdaUpdate().eq(ModelPriceOverride::getModelCode, modelCode).remove();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
