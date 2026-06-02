package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Payment;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.PaymentMapper;
import com.ycyu.istudyspotbackend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreatePaymentSuccess() {
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String paymentMethod = "alipay";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return 1;
        });

        Map<String, Object> result = paymentService.createPayment(userId, orderId, amount, paymentMethod);

        assertNotNull(result);
        assertEquals("1", result.get("paymentId"));
        assertEquals(orderId.toString(), result.get("orderId"));
        assertEquals(amount, result.get("amount"));
        assertEquals(paymentMethod, result.get("paymentMethod"));
        assertEquals("success", result.get("status"));
        assertNotNull(result.get("payTime"));
        assertNotNull(result.get("createdAt"));
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, times(1)).insert(any(Payment.class));
        verify(paymentMapper, times(1)).markAsSuccess(anyLong());
        verify(orderMapper, times(1)).updateStatus(orderId, "paid");
    }

    @Test
    public void testCreatePaymentAutoSuccess_marksPaymentAsSuccess() {
        Long userId = 1L;
        Long orderId = 2L;
        BigDecimal amount = new BigDecimal("50.00");
        String paymentMethod = "balance";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(10L);
            return 1;
        });

        Map<String, Object> result = paymentService.createPayment(userId, orderId, amount, paymentMethod);

        assertEquals("success", result.get("status"));
        verify(paymentMapper, times(1)).markAsSuccess(10L);
        verify(orderMapper, times(1)).updateStatus(orderId, "paid");
    }

    @Test
    public void testCreatePaymentAutoSuccess_paymentStatusIsSuccess() {
        Long userId = 1L;
        Long orderId = 3L;
        BigDecimal amount = new BigDecimal("30.00");
        String paymentMethod = "wechat";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(5L);
            assertEquals("success", payment.getStatus());
            assertNotNull(payment.getPayTime());
            return 1;
        });

        paymentService.createPayment(userId, orderId, amount, paymentMethod);

        verify(paymentMapper, times(1)).insert(argThat(payment ->
            "success".equals(payment.getStatus()) && payment.getPayTime() != null
        ));
    }

    @Test
    public void testCreatePaymentOrderNotFound() {
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String paymentMethod = "alipay";

        when(orderMapper.findById(orderId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("订单不存在", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
        verify(paymentMapper, never()).markAsSuccess(anyLong());
        verify(orderMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    public void testCreatePaymentOrderStatusIncorrect() {
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        String paymentMethod = "alipay";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("paid");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("订单状态不正确，无法支付", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
        verify(orderMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    public void testCreatePaymentAmountMismatch() {
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal orderAmount = new BigDecimal("200.00");
        String paymentMethod = "alipay";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(orderAmount);

        when(orderMapper.findById(orderId)).thenReturn(order);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("支付金额与订单金额不符", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
        verify(orderMapper, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    public void testCreatePayment_generatesPaymentNo() {
        Long userId = 1L;
        Long orderId = 5L;
        BigDecimal amount = new BigDecimal("80.00");
        String paymentMethod = "balance";

        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            assertTrue(payment.getPaymentNo().startsWith("PAY"));
            return 1;
        });

        paymentService.createPayment(userId, orderId, amount, paymentMethod);

        verify(paymentMapper, times(1)).insert(argThat(payment ->
            payment.getPaymentNo() != null && payment.getPaymentNo().startsWith("PAY")
        ));
    }

    @Test
    public void testGetPaymentStatusSuccess() {
        Long paymentId = 1L;

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus("success");

        when(paymentMapper.findById(paymentId)).thenReturn(payment);

        Payment result = paymentService.getPaymentStatus(paymentId);

        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertEquals("success", result.getStatus());
        verify(paymentMapper, times(1)).findById(paymentId);
    }

    @Test
    public void testGetPaymentStatusNotFound() {
        Long paymentId = 1L;

        when(paymentMapper.findById(paymentId)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentStatus(paymentId);
        });

        assertEquals("支付记录不存在", exception.getMessage());
        verify(paymentMapper, times(1)).findById(paymentId);
    }

    @Test
    public void testPayCallback_success() {
        String paymentNo = "PAY12345678901";
        boolean success = true;

        Map<String, Object> result = paymentService.payCallback(paymentNo, success);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(paymentNo, result.get("paymentNo"));
        assertEquals(true, result.get("success"));
        assertEquals("支付成功", result.get("message"));
    }

    @Test
    public void testPayCallback_failure() {
        String paymentNo = "PAY12345678901";
        boolean success = false;

        Map<String, Object> result = paymentService.payCallback(paymentNo, success);

        assertNotNull(result);
        assertEquals(paymentNo, result.get("paymentNo"));
        assertEquals(false, result.get("success"));
        assertEquals("支付失败", result.get("message"));
    }
}
