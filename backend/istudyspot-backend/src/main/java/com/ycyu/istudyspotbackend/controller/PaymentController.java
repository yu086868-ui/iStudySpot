package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.PaymentDTO;
import com.ycyu.istudyspotbackend.entity.Payment;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<Map<String, Object>> createPayment(
            @RequestBody PaymentDTO paymentDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = paymentService.createPayment(
                userId,
                paymentDTO.getOrderId(),
                paymentDTO.getAmount(),
                paymentDTO.getPaymentMethod()
        );
        return Result.success("支付订单创建成功", result);
    }

    @GetMapping("/{id}")
    public Result<Payment> getPaymentStatus(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentStatus(id);
        return Result.success("获取成功", payment);
    }

    @PostMapping("/callback")
    public Result<Map<String, Object>> payCallback(@RequestBody Map<String, Object> params) {
        String paymentNo = (String) params.get("paymentNo");
        boolean success = (boolean) params.get("success");
        Map<String, Object> result = paymentService.payCallback(paymentNo, success);
        return Result.success("回调处理成功", result);
    }
}