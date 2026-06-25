package com.keybridge.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank
        @Pattern(
                regexp = "^(?=[A-Za-z])(?=.*\\d)[A-Za-z][A-Za-z0-9_]{5,19}$",
                message = "用户名须为6到20位，以字母开头，至少包含一个数字，只能使用字母、数字和下划线"
        )
        String username,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{10,64}$",
                message = "密码须为10到64位，并同时包含大写字母、小写字母、数字和特殊字符"
        )
        String password,
        @NotBlank
        @Pattern(
                regexp = "^(?!\\d+$)[\\p{L}\\p{N}_-]{2,20}$",
                message = "昵称须为2到20位，可使用中英文、数字、下划线或短横线，且不能为纯数字"
        )
        String nickname
) {
}
