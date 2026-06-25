package com.keybridge.module.user.controller;

import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.user.dto.UserOverview;
import com.keybridge.module.user.entity.SysUser;
import com.keybridge.module.user.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private SysUserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    void promotesRegularUserToAdmin() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setRole("USER");
        when(userService.getById(7L)).thenReturn(user);

        ApiResponse<Void> response = controller.promoteToAdmin(7L, "ADMIN");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(user.getRole()).isEqualTo("ADMIN");
        verify(userService).updateById(user);
    }

    @Test
    void rejectsUserWhoIsAlreadyAdmin() {
        SysUser user = new SysUser();
        user.setId(8L);
        user.setRole("ADMIN");
        when(userService.getById(8L)).thenReturn(user);

        assertThatThrownBy(() -> controller.promoteToAdmin(8L, "ADMIN"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该用户已经是管理员");
        verify(userService, never()).updateById(user);
    }

    @Test
    void rejectsUnsupportedRole() {
        assertThatThrownBy(() -> controller.promoteToAdmin(9L, "USER"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅支持将普通用户提升为管理员");
        verify(userService, never()).getById(9L);
    }

    @Test
    void returnsSharedUserOverview() {
        UserOverview overview = new UserOverview(131, 128, 2, 129);
        when(userService.overview()).thenReturn(overview);

        ApiResponse<UserOverview> response = controller.overview();

        assertThat(response.getData()).isEqualTo(overview);
        assertThat(response.getData().activeUsers()).isEqualTo(128);
    }
}
