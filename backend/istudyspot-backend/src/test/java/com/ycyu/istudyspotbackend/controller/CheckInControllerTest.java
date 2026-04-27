package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
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
public class CheckInControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckInController checkInController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkInController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCheckIn() throws Exception {
        // 模拟签到成功
        Map<String, Object> result = new HashMap<>();
        result.put("message", "签到成功");
        when(orderService.checkin(anyLong(), anyString())).thenReturn(result);

        // 测试签到
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "1", "seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("签到成功"));

        // 验证方法调用
        verify(orderService, times(1)).checkin(anyLong(), anyString());
    }

    @Test
    void testCheckOut() throws Exception {
        // 模拟签退成功
        Map<String, Object> result = new HashMap<>();
        result.put("message", "签退成功");
        when(orderService.checkout(anyLong())).thenReturn(result);

        // 测试签退
        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkInRecordId", "1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("签退成功"));

        // 验证方法调用
        verify(orderService, times(1)).checkout(anyLong());
    }

    @Test
    void testGetCurrentCheckInStatus() throws Exception {
        // 测试获取当前签到状态
        mockMvc.perform(get("/api/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }
}