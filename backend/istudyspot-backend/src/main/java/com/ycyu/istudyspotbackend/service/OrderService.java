package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(Long userId, Long seatId, LocalDateTime startTime,
                                    LocalDateTime endTime, String bookingType);
    List<Order> getOrderList(Long userId, String status);
    Order getOrderDetail(Long orderId);
    Map<String, Object> cancelOrder(Long orderId);
    Map<String, Object> checkin(Long orderId, String checkinCode);
    Map<String, Object> checkout(Long orderId);
    Map<String, Object> renew(Long orderId, LocalDateTime newEndTime);
}