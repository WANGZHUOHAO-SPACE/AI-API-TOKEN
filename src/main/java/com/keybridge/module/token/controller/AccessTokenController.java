package com.keybridge.module.token.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.token.dto.CreateAccessTokenRequest;
import com.keybridge.module.token.dto.CreateAccessTokenResponse;
import com.keybridge.module.token.entity.AccessToken;
import com.keybridge.module.token.service.AccessTokenService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class AccessTokenController {

    private final AccessTokenService tokenService;
    private final SysUserService userService;

    @GetMapping
    public ApiResponse<Page<AccessToken>> list(Authentication authentication,
                                               @RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        Long userId = currentUser(authentication).getId();
        return ApiResponse.success(tokenService.lambdaQuery()
                .eq(AccessToken::getUserId, userId)
                .orderByDesc(AccessToken::getCreatedAt)
                .page(new Page<>(page, size)));
    }

    @PostMapping
    public ApiResponse<CreateAccessTokenResponse> create(Authentication authentication,
                                                          @Valid @RequestBody CreateAccessTokenRequest request) {
        return ApiResponse.success(tokenService.createToken(currentUser(authentication).getId(), request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(Authentication authentication, @PathVariable Long id,
                                          @RequestParam Integer status) {
        if (status != 0 && status != 1) throw new BusinessException("status 只能为 0 或 1");
        AccessToken token = ownedToken(authentication, id);
        token.setStatus(status);
        tokenService.updateById(token);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
        AccessToken token = ownedToken(authentication, id);
        tokenService.removeById(token.getId());
        return ApiResponse.success();
    }

    private AccessToken ownedToken(Authentication authentication, Long id) {
        AccessToken token = tokenService.getById(id);
        Long userId = currentUser(authentication).getId();
        if (token == null || !userId.equals(token.getUserId())) {
            throw new BusinessException(404, "平台令牌不存在");
        }
        return token;
    }

    private SysUser currentUser(Authentication authentication) {
        SysUser user = userService.findByUsername(authentication.getName());
        if (user == null) throw new BusinessException(401, "登录状态无效");
        return user;
    }
}
