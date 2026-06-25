package com.keybridge.module.auth.service;

import com.keybridge.common.exception.BusinessException;
import com.keybridge.common.security.JwtTokenProvider;
import com.keybridge.module.auth.dto.LoginRequest;
import com.keybridge.module.auth.dto.LoginResponse;
import com.keybridge.module.auth.dto.RegisterRequest;
import com.keybridge.module.auth.dto.ChangePasswordRequest;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "administrator", "root", "system", "support", "operator", "keybridge"
    );
    private static final Set<String> WEAK_PASSWORD_FRAGMENTS = Set.of(
            "password", "qwerty", "123456", "abcdef", "admin", "letmein", "welcome"
    );

    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public SysUser register(RegisterRequest request) {
        String username = request.username().trim();
        String nickname = request.nickname().trim();
        String normalizedUsername = username.toLowerCase(Locale.ROOT);
        if (RESERVED_USERNAMES.stream().anyMatch(normalizedUsername::startsWith)) {
            throw new BusinessException("该用户名属于系统保留名称，请更换用户名");
        }
        validatePasswordPolicy(request.password(), normalizedUsername);
        if (userService.findByUsername(username) != null) {
            throw new BusinessException(409, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(nickname);
        user.setRole("USER");
        user.setStatus(1);
        try {
            userService.save(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(409, "用户名已存在");
        }
        return user;
    }

    private boolean hasTripleRepeatedCharacter(String password) {
        for (int index = 2; index < password.length(); index++) {
            if (password.charAt(index) == password.charAt(index - 1)
                    && password.charAt(index) == password.charAt(index - 2)) {
                return true;
            }
        }
        return false;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException exception) {
            throw new BusinessException(403, "账号已被禁用");
        } catch (BadCredentialsException exception) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        SysUser user = userService.findByUsername(request.username());
        String token = tokenProvider.createToken(user.getUsername(), user.getRole());
        return new LoginResponse(token, "Bearer", tokenProvider.getExpirationSeconds(), user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        SysUser user = userService.findByUsername(username);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "原密码错误");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }
        validatePasswordPolicy(request.newPassword(), username.toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userService.updateById(user);
    }

    private void validatePasswordPolicy(String password, String normalizedUsername) {
        String normalizedPassword = password.toLowerCase(Locale.ROOT);
        if (normalizedPassword.contains(normalizedUsername)) {
            throw new BusinessException("密码不能包含用户名");
        }
        if (WEAK_PASSWORD_FRAGMENTS.stream().anyMatch(normalizedPassword::contains)) {
            throw new BusinessException("密码包含常见弱密码片段，请使用更复杂的密码");
        }
        if (hasTripleRepeatedCharacter(password)) {
            throw new BusinessException("密码不能连续出现三个相同字符");
        }
    }
}
