package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.WxLoginRequestDTO;
import com.ycyu.istudyspotbackend.entity.User;
import com.ycyu.istudyspotbackend.service.WxUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxUserControllerTest {

    @Mock
    private WxUserService wxUserService;

    @InjectMocks
    private WxUserController wxUserController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxUserController).build();
    }

    @Test
    void testWxLogin_Success() throws Exception {
        WxLoginRequestDTO request = new WxLoginRequestDTO();
        request.setCode("test-code");

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "test-token");
        loginResult.put("user", new HashMap<>());

        when(wxUserService.wxLogin(anyString())).thenReturn(loginResult);

        mockMvc.perform(post("/api/wx/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"));
    }

    @Test
    void testWxLogin_EmptyCode() throws Exception {
        WxLoginRequestDTO request = new WxLoginRequestDTO();
        request.setCode("");

        mockMvc.perform(post("/api/wx/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testGetUserProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setAvatar("avatar.jpg");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        when(wxUserService.getUserProfile(anyLong())).thenReturn(user);

        mockMvc.perform(get("/api/wx/user/profile")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    void testUpdateUserProfile() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("nickname", "新昵称");

        mockMvc.perform(put("/api/wx/user/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetUserHomeInfo() throws Exception {
        Map<String, Object> homeInfo = new HashMap<>();
        homeInfo.put("totalStudyTime", 120);
        homeInfo.put("todayCheckIn", true);

        when(wxUserService.getUserHomeInfo(anyLong())).thenReturn(homeInfo);

        mockMvc.perform(get("/api/wx/user/home")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalStudyTime").value(120));
    }

    @Test
    void testGetUserProfile_Error() throws Exception {
        when(wxUserService.getUserProfile(anyLong())).thenThrow(new RuntimeException("用户不存在"));

        mockMvc.perform(get("/api/wx/user/profile")
                .requestAttr("userId", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}