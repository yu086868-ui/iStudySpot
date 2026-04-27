package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // 注册 Java 8 时间模块
    }

    @Test
    void testCreateReservation() throws Exception {
        // 模拟创建订单成功
        Map<String, Object> result = new HashMap<>();
        result.put("order", new HashMap<>());
        doReturn(result).when(orderService).createOrder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(String.class));

        // 测试创建预约
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setSeatId(1L);
        bookingDTO.setStudyRoomId(1L);
        bookingDTO.setStartTime(LocalDateTime.now());
        bookingDTO.setEndTime(LocalDateTime.now().plusHours(2));

        // 测试创建预约
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("预约成功"));

        // 验证方法调用
        verify(orderService, times(1)).createOrder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void testPayOrder() throws Exception {
        // 模拟支付订单成功
        doNothing().when(orderService).markAsPaid(1L);

        // 测试支付订单
        mockMvc.perform(post("/api/reservations/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付成功"));

        // 验证方法调用
        verify(orderService, times(1)).markAsPaid(1L);
    }

    @Test
    void testCancelOrder() throws Exception {
        // 模拟取消订单成功
        doNothing().when(orderService).cancelOrder(1L);

        // 测试取消订单
        mockMvc.perform(post("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("预约已取消"));

        // 验证方法调用
        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void testGetMyReservations() throws Exception {
        // 模拟获取用户订单
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        result.put("page", 1);
        result.put("pageSize", 20);
        doReturn(result).when(orderService).getOrderList(anyLong(), any(String.class), any(String.class), any(String.class), anyInt(), anyInt());

        // 测试获取我的预约
        mockMvc.perform(get("/api/reservations/my")
                .param("page", "1")
                .param("pageSize", "20")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        // 验证方法调用
        verify(orderService, times(1)).getOrderList(anyLong(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testGetReservationDetail() throws Exception {
        // 模拟获取订单详情
        com.ycyu.istudyspotbackend.entity.Order order = new com.ycyu.istudyspotbackend.entity.Order();
        order.setId(1L);
        when(orderService.getOrderDetail(1L)).thenReturn(order);

        // 测试获取预约详情
        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        // 验证方法调用
        verify(orderService, times(1)).getOrderDetail(1L);
    }
}
