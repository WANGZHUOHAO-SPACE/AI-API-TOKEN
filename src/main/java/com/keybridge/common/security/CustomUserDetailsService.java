package com.keybridge.common.security;

import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .disabled(user.getStatus() == null || user.getStatus() != 1)
                .build();
    }
}
