package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.UserService;
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
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetUserInfoSuccess() throws Exception {
        // 模拟获取用户信息成功
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("Test User");
        when(userService.getUserInfo(anyLong())).thenReturn(user);

        // 测试获取用户信息
        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 验证方法调用
        verify(userService, times(1)).getUserInfo(1L);
    }

    @Test
    void testGetUserInfoFailure() throws Exception {
        // 模拟获取用户信息失败
        when(userService.getUserInfo(anyLong())).thenThrow(new RuntimeException("获取用户信息失败"));

        // 测试获取用户信息
        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("获取用户信息失败"));

        // 验证方法调用
        verify(userService, times(1)).getUserInfo(1L);
    }

    @Test
    void testUpdateUserInfoSuccess() throws Exception {
        // 模拟更新用户信息成功
        User user = new User();
        user.setId(1L);
        user.setNickname("Updated User");
        user.setPhone("13800138000");
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        // 测试更新用户信息
        User updateUser = new User();
        updateUser.setNickname("Updated User");
        updateUser.setPhone("13800138000");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.nickname").value("Updated User"));

        // 验证方法调用
        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoFailure() throws Exception {
        // 模拟更新用户信息失败
        when(userService.updateUserInfo(any(User.class))).thenThrow(new RuntimeException("更新用户信息失败"));

        // 测试更新用户信息
        User updateUser = new User();
        updateUser.setNickname("Updated User");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUser))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("更新用户信息失败"));

        // 验证方法调用
        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {
        // 模拟更新密码成功
        doNothing().when(userService).updatePassword(anyLong(), anyString(), anyString());

        // 测试更新密码
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPassword", "123456");
        passwordData.put("newPassword", "654321");

        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordData))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功"));

        // 验证方法调用
        verify(userService, times(1)).updatePassword(1L, "123456", "654321");
    }

    @Test
    void testUpdatePasswordFailure() throws Exception {
        // 模拟更新密码失败
        doThrow(new RuntimeException("密码修改失败")).when(userService).updatePassword(anyLong(), anyString(), anyString());

        // 测试更新密码
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("oldPassword", "123456");
        passwordData.put("newPassword", "654321");

        mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordData))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码修改失败"));

        // 验证方法调用
        verify(userService, times(1)).updatePassword(1L, "123456", "654321");
    }
}
