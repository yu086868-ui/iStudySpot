package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.service.OrderService;
import com.ycyu.istudyspotbackend.service.PaymentService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderController orderController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testCreateReservation() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("order", new HashMap<>());
        doReturn(result).when(orderService).createOrder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(String.class));

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setSeatId(1L);
        bookingDTO.setStudyRoomId(1L);
        bookingDTO.setStartTime(LocalDateTime.now());
        bookingDTO.setEndTime(LocalDateTime.now().plusHours(2));

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("预约成功"));

        verify(orderService, times(1)).createOrder(anyLong(), anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any());
    }

    @Test
    void testPayReservation_success() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("pending");
        order.setTotalPrice(new BigDecimal("20.00"));

        when(orderService.getOrderDetail(1L)).thenReturn(order);

        Map<String, Object> paymentResult = new HashMap<>();
        paymentResult.put("paymentId", "1");
        paymentResult.put("orderId", "1");
        paymentResult.put("amount", new BigDecimal("20.00"));
        paymentResult.put("paymentMethod", "balance");
        paymentResult.put("status", "success");
        paymentResult.put("payTime", "2026-10-01 10:00:00");
        paymentResult.put("createdAt", "2026-10-01 10:00:00");

        when(paymentService.createPayment(eq(1L), eq(1L), any(BigDecimal.class), eq("balance")))
                .thenReturn(paymentResult);

        mockMvc.perform(post("/api/reservations/1/pay")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付成功"))
                .andExpect(jsonPath("$.data.status").value("success"));

        verify(orderService, times(1)).getOrderDetail(1L);
        verify(paymentService, times(1)).createPayment(eq(1L), eq(1L), any(BigDecimal.class), eq("balance"));
    }

    @Test
    void testPayReservation_orderNotPending() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("paid");
        order.setTotalPrice(new BigDecimal("20.00"));

        when(orderService.getOrderDetail(1L)).thenReturn(order);

        mockMvc.perform(post("/api/reservations/1/pay")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("订单状态不正确，无法支付"));

        verify(orderService, times(1)).getOrderDetail(1L);
        verify(paymentService, never()).createPayment(anyLong(), anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    void testPayReservation_paymentServiceThrowsException() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("pending");
        order.setTotalPrice(new BigDecimal("20.00"));

        when(orderService.getOrderDetail(1L)).thenReturn(order);
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), anyString()))
                .thenThrow(new RuntimeException("支付失败"));

        mockMvc.perform(post("/api/reservations/1/pay")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("支付失败"));

        verify(paymentService, times(1)).createPayment(anyLong(), anyLong(), any(BigDecimal.class), anyString());
    }

    @Test
    void testCancelOrder() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(post("/api/reservations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("预约已取消"));

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    void testGetMyReservations() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("total", 0);
        result.put("page", 1);
        result.put("pageSize", 20);
        doReturn(result).when(orderService).getOrderList(anyLong(), any(String.class), any(String.class), any(String.class), anyInt(), anyInt());

        mockMvc.perform(get("/api/reservations/my")
                .param("page", "1")
                .param("pageSize", "20")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService, times(1)).getOrderList(anyLong(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void testGetReservationDetail() throws Exception {
        Order order = new Order();
        order.setId(1L);
        when(orderService.getOrderDetail(1L)).thenReturn(order);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(orderService, times(1)).getOrderDetail(1L);
    }

    @Test
    void testGetReservationRules() throws Exception {
        mockMvc.perform(get("/api/reservations/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.maxAdvanceDays").value(7))
                .andExpect(jsonPath("$.data.maxDailyReservations").value(2))
                .andExpect(jsonPath("$.data.maxDurationHours").value(4))
                .andExpect(jsonPath("$.data.minDurationMinutes").value(30))
                .andExpect(jsonPath("$.data.cancellationDeadlineMinutes").value(15))
                .andExpect(jsonPath("$.data.noShowPenalty").value(5));
    }
}
