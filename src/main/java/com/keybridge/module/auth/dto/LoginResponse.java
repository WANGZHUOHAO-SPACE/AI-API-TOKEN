package com.keybridge.module.auth.dto;

import com.keybridge.module.user.entity.SysUser;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, SysUser user) {
}
