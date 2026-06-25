package com.keybridge.module.provider.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.provider.entity.AiProvider;

public interface AiProviderService extends IService<AiProvider> {
    AiProvider findByCode(String code);
}
