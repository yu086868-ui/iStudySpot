package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLoginSuccess() throws Exception {
        // 模拟登录成功
        Map<String, Object> result = new HashMap<>();
        result.put("token", "test-token");
        result.put("refreshToken", "test-refresh-token");
        result.put("user", new HashMap<>());
        when(authService.login("testuser", "123456")).thenReturn(result);

        // 测试登录
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "testuser", "password", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.token").value("test-token"));

        // 验证方法调用
        verify(authService, times(1)).login("testuser", "123456");
    }

    @Test
    void testLoginFailure() throws Exception {
        // 模拟登录失败
        when(authService.login("testuser", "wrongpassword")).thenThrow(new RuntimeException("用户名或密码错误"));

        // 测试登录
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("username", "testuser", "password", "wrongpassword"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        // 验证方法调用
        verify(authService, times(1)).login("testuser", "wrongpassword");
    }

    @Test
    void testRegister() throws Exception {
        // 模拟注册成功
        Map<String, Object> result = new HashMap<>();
        result.put("userId", "1");
        when(authService.register("newuser", "123456", "New User", "13800138000", "20240001")).thenReturn(result);

        // 测试注册
        Map<String, Object> registerData = new HashMap<>();
        registerData.put("username", "newuser");
        registerData.put("password", "123456");
        registerData.put("nickname", "New User");
        registerData.put("phone", "13800138000");
        registerData.put("studentId", "20240001");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));

        // 验证方法调用
        verify(authService, times(1)).register("newuser", "123456", "New User", "13800138000", "20240001");
    }

    @Test
    void testRefreshToken() throws Exception {
        // 模拟刷新令牌成功
        Map<String, Object> result = new HashMap<>();
        result.put("token", "new-test-token");
        when(authService.refreshToken("test-refresh-token")).thenReturn(result);

        // 测试刷新令牌
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refreshToken", "test-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("刷新成功"))
                .andExpect(jsonPath("$.data.token").value("new-test-token"));

        // 验证方法调用
        verify(authService, times(1)).refreshToken("test-refresh-token");
    }

    @Test
    void testLogout() throws Exception {
        // 模拟登出成功
        doNothing().when(authService).logout(anyLong());

        // 测试登出
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));

        // 验证方法调用
        verify(authService, times(1)).logout(anyLong());
    }
}
