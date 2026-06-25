package com.keybridge.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.keybridge.module.user.dto.UserOverview;
import com.keybridge.module.user.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser findByUsername(String username);

    UserOverview overview();
}
