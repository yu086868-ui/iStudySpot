package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.ReservationRulesProvider;
import com.ycyu.istudyspotbackend.dto.WxBookingDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.service.OrderService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ReservationRulesProvider reservationRulesProvider;

    @InjectMocks
    private WxOrderController wxOrderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxOrderController).build();
    }

    @Test
    void testCreateReservation() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", 1);
        result.put("status", "pending");

        when(orderService.createOrder(anyLong(), anyLong(), anyLong(), any(), any(), anyString())).thenReturn(result);

        String body = "{\"studyRoomId\":\"1\",\"seatId\":\"1\",\"startTime\":\"2026-06-17 08:00:00\",\"endTime\":\"2026-06-17 12:00:00\",\"bookingType\":\"hourly\"}";

        mockMvc.perform(post("/api/wx/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetMyReservations() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setSeatId(1L);
        order.setRoomId(1L);
        order.setStatus("completed");
        order.setTotalPrice(new BigDecimal("40.00"));
        order.setPlanStartTime(LocalDateTime.now());
        order.setPlanEndTime(LocalDateTime.now().plusHours(4));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("list", List.of(order));
        result.put("total", 1);

        when(orderService.getOrderList(anyLong(), any(), any(), any(), anyInt(), anyInt())).thenReturn(result);

        mockMvc.perform(get("/api/wx/reservations/my")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testGetReservationDetail() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setSeatId(1L);
        order.setRoomId(1L);
        order.setStatus("completed");
        order.setTotalPrice(new BigDecimal("40.00"));
        order.setPlanStartTime(LocalDateTime.now());
        order.setPlanEndTime(LocalDateTime.now().plusHours(4));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        when(orderService.getOrderDetail(anyLong())).thenReturn(order);

        mockMvc.perform(get("/api/wx/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCancelReservation() throws Exception {
        doNothing().when(orderService).cancelOrder(anyLong());

        mockMvc.perform(post("/api/wx/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetReservationRules() throws Exception {
        Map<String, Object> rules = new HashMap<>();
        rules.put("maxAdvanceDays", 7);
        rules.put("maxDailyReservations", 2);

        when(reservationRulesProvider.getRules()).thenReturn(rules);

        mockMvc.perform(get("/api/wx/reservations/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.maxAdvanceDays").value(7));
    }
}