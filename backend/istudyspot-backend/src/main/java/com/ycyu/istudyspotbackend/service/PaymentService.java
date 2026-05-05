package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Payment;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {
    Map<String, Object> createPayment(Long userId, Long orderId, BigDecimal amount, String paymentMethod);
    Payment getPaymentStatus(Long paymentId);
    Map<String, Object> payCallback(String paymentNo, boolean success);
}