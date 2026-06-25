package com.keybridge.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{10,64}$",
                message = "新密码须为10到64位，并同时包含大写字母、小写字母、数字和特殊字符"
        )
        String newPassword
) {
}
