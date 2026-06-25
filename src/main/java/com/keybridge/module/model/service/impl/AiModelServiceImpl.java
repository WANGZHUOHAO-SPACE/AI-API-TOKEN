package com.keybridge.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.model.entity.AiModel;
import com.keybridge.module.model.mapper.AiModelMapper;
import com.keybridge.module.model.service.AiModelService;
import org.springframework.stereotype.Service;

@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {
}
