package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private OrderMapper orderMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<Seat> getSeatStatus(Long roomId) {
        return seatMapper.findByRoomId(roomId);
    }

    @Override
    public Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        long hours = java.time.Duration.between(start, end).toHours();
        if (hours < 1) hours = 1;

        BigDecimal price = new BigDecimal("15.00");
        BigDecimal totalAmount = price.multiply(new BigDecimal(hours));

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("hours", hours);
        return result;
    }

    @Override
    public Map<String, Object> createOrder(Long userId, Long seatId, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        int conflict = orderMapper.checkTimeConflict(seatId, start, end);
        if (conflict > 0) {
            throw new RuntimeException("该时段座位已被预订");
        }

        Seat seat = seatMapper.findById(seatId);

        long hours = java.time.Duration.between(start, end).toHours();
        if (hours < 1) hours = 1;
        BigDecimal totalAmount = new BigDecimal("15.00").multiply(new BigDecimal(hours));

        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + userId);
        order.setUserId(userId);
        order.setSeatId(seatId);
        order.setSeatNumber(seat.getSeatNumber());
        order.setRoomId(seat.getRoomId());
        order.setRoomName("iStudySpot自习室");
        order.setPlanStartTime(start);
        order.setPlanEndTime(end);
        order.setTotalAmount(totalAmount);
        order.setStatus(1);

        orderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("totalAmount", totalAmount);
        result.put("status", 1);
        result.put("expireTime", LocalDateTime.now().plusMinutes(15));
        return result;
    }
}