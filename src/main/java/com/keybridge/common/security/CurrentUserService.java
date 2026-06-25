package com.keybridge.common.security;

import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final SysUserService userService;

    public SysUser requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "请先登录");
        }
        SysUser user = userService.findByUsername(authentication.getName());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(401, "登录状态无效");
        }
        return user;
    }
}
