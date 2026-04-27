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
    public void testCreatePaymentSuccess() throws Exception {
        // 准备测试数据
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal(100.00);
        String paymentMethod = "alipay";

        // 模拟订单
        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);
        when(paymentMapper.insert(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L); // 设置ID
            return 1;
        });

        // 执行测试
        Map<String, Object> result = paymentService.createPayment(userId, orderId, amount, paymentMethod);

        // 验证结果
        assertNotNull(result);
        assertEquals(orderId.toString(), result.get("orderId"));
        assertEquals(amount, result.get("amount"));
        assertEquals(paymentMethod, result.get("paymentMethod"));
        assertTrue(result.containsKey("paymentUrl"));
        assertTrue(result.containsKey("createdAt"));
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, times(1)).insert(any(Payment.class));
    }

    @Test
    public void testCreatePaymentOrderNotFound() {
        // 准备测试数据
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal(100.00);
        String paymentMethod = "alipay";

        // 模拟订单不存在
        when(orderMapper.findById(orderId)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("订单不存在", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
    }

    @Test
    public void testCreatePaymentOrderStatusIncorrect() {
        // 准备测试数据
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal(100.00);
        String paymentMethod = "alipay";

        // 模拟订单状态不正确
        Order order = new Order();
        order.setId(orderId);
        order.setStatus("paid");
        order.setTotalPrice(amount);

        when(orderMapper.findById(orderId)).thenReturn(order);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("订单状态不正确，无法支付", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
    }

    @Test
    public void testCreatePaymentAmountMismatch() {
        // 准备测试数据
        Long userId = 1L;
        Long orderId = 1L;
        BigDecimal amount = new BigDecimal(100.00);
        BigDecimal orderAmount = new BigDecimal(200.00);
        String paymentMethod = "alipay";

        // 模拟订单金额不符
        Order order = new Order();
        order.setId(orderId);
        order.setStatus("pending");
        order.setTotalPrice(orderAmount);

        when(orderMapper.findById(orderId)).thenReturn(order);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(userId, orderId, amount, paymentMethod);
        });

        assertEquals("支付金额与订单金额不符", exception.getMessage());
        verify(orderMapper, times(1)).findById(orderId);
        verify(paymentMapper, never()).insert(any(Payment.class));
    }

    @Test
    public void testGetPaymentStatusSuccess() {
        // 准备测试数据
        Long paymentId = 1L;

        // 模拟支付记录
        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus("paid");

        when(paymentMapper.findById(paymentId)).thenReturn(payment);

        // 执行测试
        Payment result = paymentService.getPaymentStatus(paymentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(paymentId, result.getId());
        assertEquals("paid", result.getStatus());
        verify(paymentMapper, times(1)).findById(paymentId);
    }

    @Test
    public void testGetPaymentStatusNotFound() {
        // 准备测试数据
        Long paymentId = 1L;

        // 模拟支付记录不存在
        when(paymentMapper.findById(paymentId)).thenReturn(null);

        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentStatus(paymentId);
        });

        assertEquals("支付记录不存在", exception.getMessage());
        verify(paymentMapper, times(1)).findById(paymentId);
    }

    @Test
    public void testPayCallback() {
        // 准备测试数据
        String paymentNo = "PAY12345678901";
        boolean success = true;

        // 执行测试
        Map<String, Object> result = paymentService.payCallback(paymentNo, success);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
