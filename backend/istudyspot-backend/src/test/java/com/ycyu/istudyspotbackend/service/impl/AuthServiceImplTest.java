package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import com.ycyu.istudyspotbackend.service.AuthService;
import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("e10adc3949ba59abbe56e057f20f883e"); // MD5 of "123456"
        testUser.setStatus(1);
    }

    @Test
    void testLoginSuccess() {
        // 模拟用户查询
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(jwtUtils.generateToken(1L)).thenReturn("test-token");
        when(jwtUtils.generateRefreshToken(1L)).thenReturn("test-refresh-token");

        // 测试登录
        Map<String, Object> result = authService.login("testuser", "123456");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("token"));
        assertTrue(result.containsKey("refreshToken"));
        assertTrue(result.containsKey("user"));

        // 验证方法调用
        verify(userMapper, times(1)).findByUsername("testuser");
        verify(jwtUtils, times(1)).generateToken(1L);
        verify(jwtUtils, times(1)).generateRefreshToken(1L);
    }

    @Test
    void testLoginUserNotFound() {
        // 模拟用户不存在
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        // 测试登录
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("nonexistent", "123456");
        });

        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证方法调用
        verify(userMapper, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testLoginWrongPassword() {
        // 模拟用户存在但密码错误
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        // 测试登录
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("testuser", "wrongpassword");
        });

        assertEquals("用户名或密码错误", exception.getMessage());

        // 验证方法调用
        verify(userMapper, times(1)).findByUsername("testuser");
    }

    @Test
    void testRegister() {
        // 模拟用户不存在
        when(userMapper.findByUsername("newuser")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L); // 设置ID
            return 1;
        });

        // 测试注册
        Map<String, Object> result = authService.register("newuser", "123456", "New User", "13800138000", "20240001");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("userId"));

        // 验证方法调用
        verify(userMapper, times(1)).findByUsername("newuser");
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegisterUserExists() {
        // 模拟用户已存在
        when(userMapper.findByUsername("existinguser")).thenReturn(testUser);

        // 测试注册
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register("existinguser", "123456", "Existing User", "13800138000", "20240001");
        });

        assertEquals("用户名已存在", exception.getMessage());

        // 验证方法调用
        verify(userMapper, times(1)).findByUsername("existinguser");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRefreshToken() {
        // 测试刷新令牌
        when(jwtUtils.getUserIdFromToken("test-refresh-token")).thenReturn(1L);
        when(jwtUtils.generateToken(1L)).thenReturn("new-test-token");
        when(jwtUtils.generateRefreshToken(1L)).thenReturn("new-test-refresh-token");

        Map<String, Object> result = authService.refreshToken("test-refresh-token");

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("token"));

        // 验证方法调用
        verify(jwtUtils, times(1)).getUserIdFromToken("test-refresh-token");
        verify(jwtUtils, times(1)).generateToken(1L);
        verify(jwtUtils, times(1)).generateRefreshToken(1L);
    }

    @Test
    void testLogout() {
        // 测试登出
        authService.logout(1L);

        // 验证方法调用
        verify(userMapper, never()).findByUsername(anyString());
    }
}
