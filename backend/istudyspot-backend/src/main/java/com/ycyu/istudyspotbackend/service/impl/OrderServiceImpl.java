package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long seatId, LocalDateTime startTime,
                                           LocalDateTime endTime, String bookingType) {
        // 检查座位是否存在
        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        // 检查时间冲突
        int conflict = orderMapper.checkTimeConflict(seatId, startTime, endTime);
        if (conflict > 0) {
            throw new RuntimeException("该时段座位已被预订");
        }

        // 计算价格
        long hours = java.time.Duration.between(startTime, endTime).toMinutes() / 60;
        if (hours < 1) hours = 1;
        BigDecimal totalPrice = seat.getPricePerHour().multiply(new BigDecimal(hours));

        // 获取自习室信息
        StudyRoom room = studyRoomMapper.findById(seat.getRoomId());

        // 创建订单
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + userId);
        order.setUserId(userId);
        order.setSeatId(seatId);
        order.setRoomId(seat.getRoomId());
        order.setStudyRoomName(room.getName());
        order.setSeatPosition(seat.getRowNum() + "-" + seat.getColNum());
        order.setStartTime(startTime);
        order.setEndTime(endTime);
        order.setTotalPrice(totalPrice);
        order.setStatus("pending");

        orderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId().toString());
        result.put("seatId", seatId.toString());
        result.put("userId", userId.toString());
        result.put("startTime", startTime.format(formatter));
        result.put("endTime", endTime.format(formatter));
        result.put("totalPrice", totalPrice);
        result.put("status", "pending");
        result.put("createdAt", LocalDateTime.now().format(formatter));
        return result;
    }

    @Override
    public Map<String, Object> getOrderList(Long userId, String status, String startDate, String endDate, int page, int pageSize) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderMapper.findByUserIdAndStatus(userId, status);
        } else {
            orders = orderMapper.findByUserId(userId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);
        result.put("total", orders.size());
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Order getOrderDetail(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            throw new RuntimeException("当前状态无法取消");
        }

        orderMapper.updateStatus(orderId, "cancelled");
    }

    @Override
    @Transactional
    public Map<String, Object> checkin(Long orderId, String checkinCode) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"paid".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法签到");
        }

        orderMapper.checkin(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", orderId.toString());
        result.put("checkinTime", LocalDateTime.now().format(formatter));
        result.put("status", "in_use");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> checkout(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"in_use".equals(order.getStatus())) {
            throw new RuntimeException("订单未在使用中");
        }

        LocalDateTime now = LocalDateTime.now();
        int duration = (int) java.time.Duration.between(order.getCheckinTime(), now).toMinutes();
        BigDecimal actualPrice = order.getTotalPrice();

        orderMapper.checkout(orderId, duration, actualPrice);

        Map<String, Object> result = new HashMap<>();
        result.put("id", orderId.toString());
        result.put("checkoutTime", now.format(formatter));
        result.put("actualDuration", duration);
        result.put("actualPrice", actualPrice);
        result.put("status", "completed");
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> renew(Long orderId, LocalDateTime newEndTime) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"in_use".equals(order.getStatus())) {
            throw new RuntimeException("订单未在使用中");
        }

        // 计算续费金额
        long additionalMinutes = java.time.Duration.between(order.getEndTime(), newEndTime).toMinutes();
        if (additionalMinutes <= 0) {
            throw new RuntimeException("新结束时间必须晚于原结束时间");
        }

        Seat seat = seatMapper.findById(order.getSeatId());
        BigDecimal additionalAmount = seat.getPricePerHour()
                .multiply(new BigDecimal(additionalMinutes))
                .divide(new BigDecimal("60"), 2, BigDecimal.ROUND_HALF_UP);

        // 更新订单
        order.setEndTime(newEndTime);
        order.setTotalPrice(order.getTotalPrice().add(additionalAmount));
        orderMapper.updateStatus(orderId, "in_use");

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId.toString());
        result.put("additionalAmount", additionalAmount);
        result.put("newEndTime", newEndTime.format(formatter));
        return result;
    }
}