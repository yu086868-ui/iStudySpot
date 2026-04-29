package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.entity.Payment;
import com.ycyu.istudyspotbackend.service.PaymentService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreatePaymentSuccess() throws Exception {
        // 模拟创建支付成功
        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", 1L);
        result.put("paymentUrl", "https://example.com/pay");
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), anyString())).thenReturn(result);

        // 测试创建支付
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderId", 1L);
        paymentData.put("amount", 100.0);
        paymentData.put("paymentMethod", "alipay");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentData))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("支付订单创建成功"))
                .andExpect(jsonPath("$.data.paymentId").value(1));

        // 验证方法调用
        verify(paymentService, times(1)).createPayment(1L, 1L, BigDecimal.valueOf(100.0), "alipay");
    }

    @Test
    void testCreatePaymentFailure() throws Exception {
        // 模拟创建支付失败
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), anyString())).thenThrow(new RuntimeException("创建支付失败"));

        // 测试创建支付
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderId", 1L);
        paymentData.put("amount", 100.0);
        paymentData.put("paymentMethod", "alipay");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentData))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("创建支付失败"));

        // 验证方法调用
        verify(paymentService, times(1)).createPayment(1L, 1L, BigDecimal.valueOf(100.0), "alipay");
    }

    @Test
    void testGetPaymentStatusSuccess() throws Exception {
        // 模拟查询支付状态成功
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus("success");
        when(paymentService.getPaymentStatus(anyLong())).thenReturn(payment);

        // 测试查询支付状态
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("获取成功"))
                .andExpect(jsonPath("$.data.status").value("success"));

        // 验证方法调用
        verify(paymentService, times(1)).getPaymentStatus(1L);
    }

    @Test
    void testGetPaymentStatusFailure() throws Exception {
        // 模拟查询支付状态失败
        when(paymentService.getPaymentStatus(anyLong())).thenThrow(new RuntimeException("查询支付状态失败"));

        // 测试查询支付状态
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("查询支付状态失败"));

        // 验证方法调用
        verify(paymentService, times(1)).getPaymentStatus(1L);
    }

    @Test
    void testPayCallbackSuccess() throws Exception {
        // 模拟支付回调成功
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "回调处理成功");
        when(paymentService.payCallback(anyString(), anyBoolean())).thenReturn(result);

        // 测试支付回调
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("paymentNo", "pay123");
        callbackData.put("success", true);

        mockMvc.perform(post("/api/payments/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("回调处理成功"))
                .andExpect(jsonPath("$.data.status").value("success"));

        // 验证方法调用
        verify(paymentService, times(1)).payCallback("pay123", true);
    }

    @Test
    void testPayCallbackFailure() throws Exception {
        // 模拟支付回调失败
        when(paymentService.payCallback(anyString(), anyBoolean())).thenThrow(new RuntimeException("回调处理失败"));

        // 测试支付回调
        Map<String, Object> callbackData = new HashMap<>();
        callbackData.put("paymentNo", "pay123");
        callbackData.put("success", false);

        mockMvc.perform(post("/api/payments/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("回调处理失败"));

        // 验证方法调用
        verify(paymentService, times(1)).payCallback("pay123", false);
    }
}
