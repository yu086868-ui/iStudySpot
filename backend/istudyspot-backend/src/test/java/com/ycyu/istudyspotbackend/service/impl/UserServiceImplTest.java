package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("e10adc3949ba59abbe56e057f20f883e"); // MD5 of "123456"
        testUser.setEmail("testuser@example.com");
        testUser.setStatus(1);
        testUser.setCreditScore(100);
    }

    @Test
    void testGetUserInfo() {
        // 模拟用户查询
        when(userMapper.findById(1L)).thenReturn(testUser);

        // 测试获取用户
        User user = userService.getUserInfo(1L);

        // 验证结果
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());

        // 验证方法调用
        verify(userMapper, times(1)).findById(1L);
    }

    @Test
    void testUpdateUserInfo() {
        // 模拟用户查询
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenReturn(1);

        // 测试更新用户
        testUser.setUsername("newname");
        testUser.setEmail("newemail@example.com");
        User updatedUser = userService.updateUserInfo(testUser);

        // 验证结果
        assertNotNull(updatedUser);
        assertEquals("newname", updatedUser.getUsername());
        assertEquals("newemail@example.com", updatedUser.getEmail());

        // 验证方法调用
        verify(userMapper, times(1)).findById(1L);
        verify(userMapper, times(1)).update(any(User.class));
    }

    @Test
    void testUpdatePassword() {
        // 模拟用户查询
        when(userMapper.findById(anyLong())).thenReturn(testUser);
        when(userMapper.updatePassword(anyLong(), anyString())).thenReturn(1);

        // 测试更新密码
        userService.updatePassword(1L, "123456", "newpassword");

        // 验证方法调用
        verify(userMapper, times(1)).findById(anyLong());
        verify(userMapper, times(1)).updatePassword(anyLong(), anyString());
    }

    @Test
    void testUpdatePasswordWrongOldPassword() {
        // 模拟用户查询
        when(userMapper.findById(1L)).thenReturn(testUser);

        // 测试更新密码（旧密码错误）
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword(1L, "wrongpassword", "newpassword");
        });

        assertEquals("旧密码错误", exception.getMessage());

        // 验证方法调用
        verify(userMapper, times(1)).findById(1L);
        verify(userMapper, never()).updatePassword(anyLong(), anyString());
    }
}
