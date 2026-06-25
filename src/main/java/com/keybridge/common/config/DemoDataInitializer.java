package com.keybridge.common.config;

import com.keybridge.module.provider.dto.CredentialRequest;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.service.AiProviderService;
import com.keybridge.module.provider.service.ProviderCredentialService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final int MIN_DEMO_USER_COUNT = 128;
    private static final String DEMO_MEMBER_PASSWORD = "DemoMember123!";

    private final SysUserService userService;
    private final AiProviderService providerService;
    private final ProviderCredentialService credentialService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SysUser admin = ensureUser("admin", "系统管理员", "ADMIN", "admin123456");
        SysUser demoUser = ensureUser("demo_user", "演示用户", "USER", "demo123456");
        ensureDemoMembers();
        AiProvider provider = ensureProvider();
        ensureCredential(admin, provider, "管理员 Mock Key", "sk-mock-admin-demo");
        ensureCredential(demoUser, provider, "用户 Mock Key", "sk-mock-user-demo");
    }

    void ensureDemoMembers() {
        long missingUsers = Math.max(0, MIN_DEMO_USER_COUNT - userService.count());
        if (missingUsers == 0) {
            return;
        }
        List<SysUser> newUsers = new ArrayList<>();
        String passwordHash = null;
        String[] teams = {"模型研发", "接口测试", "数据分析", "应用开发", "平台运维", "课程实践"};
        for (int index = 1; newUsers.size() < missingUsers; index++) {
            String username = "demo_member_" + String.format("%03d", index);
            if (userService.findByUsername(username) != null) {
                continue;
            }
            if (passwordHash == null) {
                passwordHash = passwordEncoder.encode(DEMO_MEMBER_PASSWORD);
            }
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setNickname(teams[(index - 1) % teams.length] + "用户" + String.format("%03d", index));
            user.setPasswordHash(passwordHash);
            user.setRole("USER");
            user.setStatus(1);
            newUsers.add(user);
        }
        if (!newUsers.isEmpty()) {
            userService.saveBatch(newUsers, 50);
        }
    }

    private SysUser ensureUser(String username, String nickname, String role, String password) {
        SysUser user = userService.findByUsername(username);
        boolean isNew = user == null;
        if (isNew) {
            user = new SysUser();
            user.setUsername(username);
        }
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(1);
        user.setPasswordHash(passwordEncoder.encode(password));
        if (isNew) {
            userService.save(user);
        } else {
            userService.updateById(user);
        }
        return user;
    }

    private AiProvider ensureProvider() {
        AiProvider provider = providerService.findByCode("openai");
        boolean isNew = provider == null;
        if (isNew) {
            provider = new AiProvider();
        }
        provider.setName("OpenAI Compatible");
        provider.setCode("openai");
        provider.setBaseUrl("https://api.openai.com/v1");
        provider.setProtocolType("OPENAI_COMPATIBLE");
        provider.setStatus(1);
        provider.setTimeoutSeconds(60);
        provider.setDescription("Mock 演示及 OpenAI Compatible 调用服务商");
        if (isNew) {
            providerService.save(provider);
        } else {
            providerService.updateById(provider);
        }
        return provider;
    }

    private void ensureCredential(SysUser owner, AiProvider provider, String name, String apiKey) {
        ProviderCredential existing = credentialService.lambdaQuery()
                .eq(ProviderCredential::getUserId, owner.getId())
                .eq(ProviderCredential::getProviderId, provider.getId())
                .eq(ProviderCredential::getName, name)
                .one();
        CredentialRequest request = new CredentialRequest(provider.getId(), name, apiKey, 0, 1, 1, null);
        if (existing == null) {
            credentialService.createCredential(request, owner);
        } else {
            credentialService.updateCredential(existing.getId(), request, owner);
        }
    }
}
