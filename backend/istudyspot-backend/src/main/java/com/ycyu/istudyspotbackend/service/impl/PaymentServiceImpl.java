package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Payment;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.PaymentMapper;
import com.ycyu.istudyspotbackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderMapper orderMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> createPayment(Long userId, Long orderId, BigDecimal amount, String paymentMethod) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }
        if (amount.compareTo(order.getTotalPrice()) != 0) {
            throw new RuntimeException("支付金额与订单金额不符");
        }

        Payment payment = new Payment();
        payment.setPaymentNo("PAY" + System.currentTimeMillis() + userId);
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("pending");
        payment.setPaymentUrl("https://example.com/pay/" + payment.getPaymentNo());

        paymentMapper.insert(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId().toString());
        result.put("orderId", orderId.toString());
        result.put("amount", amount);
        result.put("paymentMethod", paymentMethod);
        result.put("paymentUrl", payment.getPaymentUrl());
        result.put("createdAt", LocalDateTime.now().format(formatter));
        return result;
    }

    @Override
    public Payment getPaymentStatus(Long paymentId) {
        Payment payment = paymentMapper.findById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }
        return payment;
    }

    @Override
    @Transactional
    public Map<String, Object> payCallback(String paymentNo, boolean success) {
        // 实际应该根据 paymentNo 查询支付记录
        // 这里简化处理
        return new HashMap<>();
    }
}