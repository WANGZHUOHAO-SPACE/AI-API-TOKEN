package com.keybridge.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.user.dto.UserOverview;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SysUserService userService;

    @GetMapping("/overview")
    public ApiResponse<UserOverview> overview() {
        return ApiResponse.success(userService.overview());
    }

    @GetMapping
    public ApiResponse<Page<SysUser>> list(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String username) {
        Page<SysUser> result = userService.lambdaQuery()
                .like(username != null && !username.isBlank(), SysUser::getUsername, username)
                .orderByDesc(SysUser::getCreatedAt)
                .page(new Page<>(page, size));
        return ApiResponse.success(result);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("status 只能为 0 或 1");
        }
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> promoteToAdmin(@PathVariable Long id, @RequestParam String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new BusinessException("仅支持将普通用户提升为管理员");
        }
        SysUser user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BusinessException("该用户已经是管理员");
        }
        user.setRole("ADMIN");
        userService.updateById(user);
        return ApiResponse.success();
    }
}
