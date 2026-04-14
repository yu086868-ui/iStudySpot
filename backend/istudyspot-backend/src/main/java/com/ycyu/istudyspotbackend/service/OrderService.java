package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Map<String, Object> createOrder(Long userId, Long seatId, LocalDateTime startTime,
                                    LocalDateTime endTime, String bookingType);
    Map<String, Object> getOrderList(Long userId, String status, String startDate, String endDate, int page, int pageSize);
    Order getOrderDetail(Long orderId);
    void cancelOrder(Long orderId);
    Map<String, Object> checkin(Long orderId, String checkinCode);
    Map<String, Object> checkout(Long orderId);
    Map<String, Object> renew(Long orderId, LocalDateTime newEndTime);
    void markAsPaid(Long orderId);
}