package com.keybridge.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keybridge.module.user.dto.UserOverview;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.mapper.SysUserMapper;
import com.keybridge.module.user.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser findByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    @Override
    public UserOverview overview() {
        long totalUsers = count();
        long activeUsers = lambdaQuery().eq(SysUser::getStatus, 1).count();
        long adminUsers = lambdaQuery().eq(SysUser::getRole, "ADMIN").count();
        return new UserOverview(totalUsers, activeUsers, adminUsers, Math.max(0, totalUsers - adminUsers));
    }
}
