package com.keybridge.common.config;

import com.keybridge.module.provider.service.AiProviderService;
import com.keybridge.module.provider.service.ProviderCredentialService;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataInitializerTest {

    @Mock
    private SysUserService userService;
    @Mock
    private AiProviderService providerService;
    @Mock
    private ProviderCredentialService credentialService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private DemoDataInitializer initializer;

    @Test
    void createsOneHundredTwentySixUniqueDemoMembers() {
        when(userService.count()).thenReturn(2L);
        when(userService.findByUsername(anyString())).thenReturn(null);
        when(passwordEncoder.encode("DemoMember123!")).thenReturn("encoded-password");

        initializer.ensureDemoMembers();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SysUser>> usersCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(userService).saveBatch(usersCaptor.capture(), eq(50));
        Collection<SysUser> users = usersCaptor.getValue();
        assertThat(users).hasSize(126);
        assertThat(users).extracting(SysUser::getUsername).doesNotHaveDuplicates();
        assertThat(users).allSatisfy(user -> {
            assertThat(user.getRole()).isEqualTo("USER");
            assertThat(user.getStatus()).isEqualTo(1);
            assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        });
    }
}
