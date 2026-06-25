package com.keybridge.module.token.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.token.entity.TokenModelPermission;
import com.keybridge.module.token.mapper.TokenModelPermissionMapper;
import com.keybridge.module.token.service.TokenModelPermissionService;
import org.springframework.stereotype.Service;

@Service
public class TokenModelPermissionServiceImpl extends ServiceImpl<TokenModelPermissionMapper, TokenModelPermission> implements TokenModelPermissionService {
}
