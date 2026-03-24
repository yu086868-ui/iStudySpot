package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Order> getOrderList(Long userId, Integer status) {
        return orderMapper.findByUserId(userId);
    }

    @Override
    public Order getOrderDetail(Long orderId) {
        return orderMapper.findById(orderId);
    }

    @Override
    public Map<String, Object> payOrder(Long orderId, Integer payType) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }

        orderMapper.updatePayStatus(orderId, 2, payType, order.getTotalAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", 2);
        result.put("payTime", LocalDateTime.now());
        return result;
    }

    @Override
    public Map<String, Object> cancelOrder(Long orderId, String reason) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new RuntimeException("当前状态无法取消");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("message", "订单已取消");
        return result;
    }

    @Override
    public Map<String, Object> checkin(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态不正确，无法签到");
        }

        orderMapper.checkin(orderId, 3);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", 3);
        result.put("actualStartTime", LocalDateTime.now());
        return result;
    }

    @Override
    public Map<String, Object> checkout(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单未在使用中");
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(order.getActualStartTime(), now);
        BigDecimal totalHours = new BigDecimal(duration.toMinutes()).divide(new BigDecimal("60"), 1, BigDecimal.ROUND_HALF_UP);

        orderMapper.checkout(orderId, 4, totalHours);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", 4);
        result.put("actualEndTime", now);
        result.put("totalHours", totalHours);
        return result;
    }
}