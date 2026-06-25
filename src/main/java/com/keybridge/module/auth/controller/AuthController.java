package com.keybridge.module.auth.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.auth.dto.LoginRequest;
import com.keybridge.module.auth.dto.LoginResponse;
import com.keybridge.module.auth.dto.RegisterRequest;
import com.keybridge.module.auth.dto.ChangePasswordRequest;
import com.keybridge.module.auth.service.AuthService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService userService;

    @PostMapping("/register")
    public ApiResponse<SysUser> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/profile")
    public ApiResponse<SysUser> profile(Authentication authentication) {
        SysUser user = userService.findByUsername(authentication.getName());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return ApiResponse.success(user);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(Authentication authentication,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ApiResponse.success();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
