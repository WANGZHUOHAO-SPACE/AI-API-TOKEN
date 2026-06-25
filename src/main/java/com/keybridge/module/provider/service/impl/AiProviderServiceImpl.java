package com.keybridge.module.provider.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.mapper.AiProviderMapper;
import com.keybridge.module.provider.service.AiProviderService;
import org.springframework.stereotype.Service;

@Service
public class AiProviderServiceImpl extends ServiceImpl<AiProviderMapper, AiProvider> implements AiProviderService {

    @Override
    public AiProvider findByCode(String code) {
        return lambdaQuery().eq(AiProvider::getCode, code).one();
    }
}
