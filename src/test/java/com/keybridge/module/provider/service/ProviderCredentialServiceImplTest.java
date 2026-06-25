package com.keybridge.module.provider.service;

import com.keybridge.common.crypto.AesGcmService;
import com.keybridge.common.crypto.CryptoProperties;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.provider.dto.CredentialRequest;
import com.keybridge.module.provider.dto.CredentialResponse;
import com.keybridge.module.provider.entity.AiProvider;
import com.keybridge.module.provider.entity.ProviderCredential;
import com.keybridge.module.provider.mapper.ProviderCredentialMapper;
import com.keybridge.module.provider.service.impl.ProviderCredentialServiceImpl;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProviderCredentialServiceImplTest {

    private ProviderCredentialMapper mapper;
    private AiProviderService providerService;
    private SysUserService userService;
    private AesGcmService aesGcmService;
    private ProviderCredentialServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(ProviderCredentialMapper.class);
        providerService = mock(AiProviderService.class);
        userService = mock(SysUserService.class);
        aesGcmService = new AesGcmService(new CryptoProperties("unit-test-encryption-key"));
        service = new ProviderCredentialServiceImpl(aesGcmService, providerService, userService);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
    }

    @Test
    void shouldEncryptKeyBindOwnerAndOnlyReturnMaskedValue() {
        SysUser owner = user(10L, "alice", "USER");
        when(providerService.getById(2L)).thenReturn(new AiProvider());
        when(userService.getById(10L)).thenReturn(owner);
        when(mapper.insert(any(ProviderCredential.class))).thenAnswer(invocation -> {
            ProviderCredential credential = invocation.getArgument(0);
            credential.setId(100L);
            return 1;
        });

        CredentialResponse response = service.createCredential(
                new CredentialRequest(2L, "My OpenAI Key", "sk-test-1234abcd", 0, 1, 1, null), owner);

        ArgumentCaptor<ProviderCredential> captor = ArgumentCaptor.forClass(ProviderCredential.class);
        verify(mapper).insert(captor.capture());
        ProviderCredential stored = captor.getValue();

        assertThat(stored.getUserId()).isEqualTo(10L);
        assertThat(stored.getEncryptedKey()).isNotEqualTo("sk-test-1234abcd");
        assertThat(aesGcmService.decrypt(stored.getEncryptedKey())).isEqualTo("sk-test-1234abcd");
        assertThat(response.maskedKey()).isEqualTo("********abcd");
        assertThat(response.toString()).doesNotContain("sk-test-1234abcd", stored.getEncryptedKey());
    }

    @Test
    void shouldHideOtherUsersCredentialFromRegularUser() {
        ProviderCredential credential = credentialOwnedBy(20L);
        when(mapper.selectById(5L)).thenReturn(credential);

        assertThatThrownBy(() -> service.getCredential(5L, user(10L, "alice", "USER")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("API Key 不存在");
    }

    @Test
    void shouldAllowAdminToViewMaskedCredential() {
        ProviderCredential credential = credentialOwnedBy(20L);
        when(mapper.selectById(5L)).thenReturn(credential);
        when(userService.getById(20L)).thenReturn(user(20L, "bob", "USER"));

        CredentialResponse response = service.getCredential(5L, user(1L, "admin", "ADMIN"));

        assertThat(response.ownerUsername()).isEqualTo("bob");
        assertThat(response.maskedKey()).isEqualTo("********a1b2");
    }

    @Test
    void shouldOnlyResolveActiveCredentialOwnedByCallingUser() {
        ProviderCredential credential = credentialOwnedBy(20L);
        credential.setStatus(1);
        when(mapper.selectById(5L)).thenReturn(credential);

        assertThat(service.requireOwnedActiveCredential(5L, 20L)).isSameAs(credential);
        assertThatThrownBy(() -> service.requireOwnedActiveCredential(5L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("API Key 不存在");
    }

    private ProviderCredential credentialOwnedBy(Long userId) {
        ProviderCredential credential = new ProviderCredential();
        credential.setId(5L);
        credential.setUserId(userId);
        credential.setProviderId(2L);
        credential.setName("Stored Key");
        credential.setEncryptedKey("ciphertext");
        credential.setKeySuffix("a1b2");
        return credential;
    }

    private SysUser user(Long id, String username, String role) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }
}
