package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    List<Order> getOrderList(Long userId, Integer status);
    Order getOrderDetail(Long orderId);
    Map<String, Object> payOrder(Long orderId, Integer payType);
    Map<String, Object> cancelOrder(Long orderId, String reason);
    Map<String, Object> checkin(Long orderId);
    Map<String, Object> checkout(Long orderId);
}