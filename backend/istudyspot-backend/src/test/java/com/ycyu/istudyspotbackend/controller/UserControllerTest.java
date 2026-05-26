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
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("Test User");
        user.setAvatar("avatar.png");
        user.setPhone("13800138000");
        user.setEmail("test@test.com");
        user.setStudentId("2024001");
        user.setCreditScore(100);
        user.setBalance(java.math.BigDecimal.valueOf(50.0));
        user.setPoints(200);
        when(userService.getUserInfo(anyLong())).thenReturn(user);

        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.avatar").value("avatar.png"))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.studentId").value("2024001"))
                .andExpect(jsonPath("$.data.creditScore").value(100))
                .andExpect(jsonPath("$.data.balance").value(50.0))
                .andExpect(jsonPath("$.data.points").value(200));

        verify(userService, times(1)).getUserInfo(1L);
    }

    @Test
    void testGetUserInfoFailure() throws Exception {
        when(userService.getUserInfo(anyLong())).thenThrow(new RuntimeException("获取用户信息失败"));

        mockMvc.perform(get("/api/users/me")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("获取用户信息失败"));

        verify(userService, times(1)).getUserInfo(1L);
    }

    @Test
    void testUpdateUserInfoWithNickname() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setNickname("Updated Nickname");
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        Map<String, Object> params = new HashMap<>();
        params.put("nickname", "Updated Nickname");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoWithAvatar() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setAvatar("new_avatar.png");
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        Map<String, Object> params = new HashMap<>();
        params.put("avatar", "new_avatar.png");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoWithPhone() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setPhone("13900139000");
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        Map<String, Object> params = new HashMap<>();
        params.put("phone", "13900139000");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoWithEmail() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail("new@test.com");
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        Map<String, Object> params = new HashMap<>();
        params.put("email", "new@test.com");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoWithAllFields() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setNickname("New Nick");
        user.setAvatar("new_avatar.png");
        user.setPhone("13900139000");
        user.setEmail("new@test.com");
        user.setStudentId("2024001");
        user.setCreditScore(100);
        user.setBalance(java.math.BigDecimal.valueOf(50.0));
        user.setPoints(200);
        when(userService.updateUserInfo(any(User.class))).thenReturn(user);

        Map<String, Object> params = new HashMap<>();
        params.put("nickname", "New Nick");
        params.put("avatar", "new_avatar.png");
        params.put("phone", "13900139000");
        params.put("email", "new@test.com");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("更新成功"))
                .andExpect(jsonPath("$.data.nickname").value("New Nick"))
                .andExpect(jsonPath("$.data.avatar").value("new_avatar.png"))
                .andExpect(jsonPath("$.data.phone").value("13900139000"))
                .andExpect(jsonPath("$.data.email").value("new@test.com"));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdateUserInfoFailure() throws Exception {
        when(userService.updateUserInfo(any(User.class))).thenThrow(new RuntimeException("更新用户信息失败"));

        Map<String, Object> params = new HashMap<>();
        params.put("nickname", "Updated User");

        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("更新用户信息失败"));

        verify(userService, times(1)).updateUserInfo(any(User.class));
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {
        doNothing().when(userService).updatePassword(anyLong(), anyString(), anyString());

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

        verify(userService, times(1)).updatePassword(1L, "123456", "654321");
    }

    @Test
    void testUpdatePasswordFailure() throws Exception {
        doThrow(new RuntimeException("密码修改失败")).when(userService).updatePassword(anyLong(), anyString(), anyString());

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

        verify(userService, times(1)).updatePassword(1L, "123456", "654321");
    }
}
