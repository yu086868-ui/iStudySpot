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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long studyRoomId, Long seatId, LocalDateTime startTime,
                                           LocalDateTime endTime, String bookingType) {
        Seat seat = seatMapper.findById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        int conflict = orderMapper.checkTimeConflict(seatId, startTime, endTime);
        if (conflict > 0) {
            throw new RuntimeException("该时段座位已被预约");
        }

        long hours = java.time.Duration.between(startTime, endTime).toMinutes() / 60;
        if (hours < 1) {
            hours = 1;
        }
        BigDecimal totalPrice = seat.getPricePerHour().multiply(new BigDecimal(hours));

        StudyRoom room = studyRoomMapper.findById(studyRoomId);

        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + userId);
        order.setUserId(userId);
        order.setSeatId(seatId);
        order.setRoomId(studyRoomId);
        order.setStudyRoomName(room.getName());
        order.setRoomName(room.getName());
        order.setSeatPosition(seat.getRowNum() + "-" + seat.getColNum());
        order.setSeatNumber(seat.getSeatNumber());
        order.setStartTime(startTime);
        order.setEndTime(endTime);
        order.setTotalPrice(totalPrice);
        order.setTotalAmount(totalPrice);
        order.setStatus("pending");

        orderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId().toString());
        result.put("studyRoomId", studyRoomId.toString());
        result.put("seatId", seatId.toString());
        result.put("userId", userId.toString());
        result.put("startTime", startTime.format(FORMATTER));
        result.put("endTime", endTime.format(FORMATTER));
        result.put("status", "pending");
        result.put("totalPrice", totalPrice);
        result.put("checkInTime", null);
        result.put("checkOutTime", null);
        result.put("createdAt", LocalDateTime.now().format(FORMATTER));
        result.put("updatedAt", LocalDateTime.now().format(FORMATTER));
        return result;
    }

    @Override
    public Map<String, Object> getOrderList(Long userId, String status, String startDate, String endDate, int page, int pageSize) {
        List<Order> orders = (status != null && !status.isEmpty())
                ? orderMapper.findByUserIdAndStatus(userId, status)
                : orderMapper.findByUserId(userId);
        return buildPagedResult(orders, page, pageSize);
    }

    @Override
    public Map<String, Object> getAdminOrderList(String keyword, String status, int page, int pageSize) {
        return buildPagedResult(orderMapper.findForAdmin(keyword, status), page, pageSize);
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
            throw new RuntimeException("订单状态不正确，无法签到，当前状态: " + order.getStatus());
        }

        orderMapper.checkin(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", orderId.toString());
        result.put("checkinTime", LocalDateTime.now().format(FORMATTER));
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
            throw new RuntimeException("订单未在使用中，当前状态: " + order.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkinTime = order.getActualStartTime() != null ? order.getActualStartTime() : order.getCheckinTime();
        if (checkinTime == null) {
            throw new RuntimeException("签到时间不存在");
        }

        int duration = (int) java.time.Duration.between(checkinTime, now).toMinutes();
        BigDecimal actualPrice = order.getTotalPrice();
        orderMapper.checkout(orderId, duration, actualPrice);

        Map<String, Object> result = new HashMap<>();
        result.put("id", orderId.toString());
        result.put("checkoutTime", now.format(FORMATTER));
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

        long additionalMinutes = java.time.Duration.between(order.getEndTime(), newEndTime).toMinutes();
        if (additionalMinutes <= 0) {
            throw new RuntimeException("新的结束时间必须晚于原结束时间");
        }

        Seat seat = seatMapper.findById(order.getSeatId());
        BigDecimal additionalAmount = seat.getPricePerHour()
                .multiply(new BigDecimal(additionalMinutes))
                .divide(new BigDecimal("60"), 2, BigDecimal.ROUND_HALF_UP);

        order.setEndTime(newEndTime);
        order.setTotalPrice(order.getTotalPrice().add(additionalAmount));
        order.setTotalAmount(order.getTotalAmount().add(additionalAmount));
        orderMapper.updateRenew(orderId, newEndTime, order.getTotalPrice(), order.getTotalAmount());

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId.toString());
        result.put("additionalAmount", additionalAmount);
        result.put("newEndTime", newEndTime.format(FORMATTER));
        return result;
    }

    @Override
    @Transactional
    public void markAsPaid(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }

        orderMapper.updateStatus(orderId, "paid");
    }

    private Map<String, Object> buildPagedResult(List<Order> orders, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePage - 1) * safePageSize, orders.size());
        int toIndex = Math.min(fromIndex + safePageSize, orders.size());

        Map<String, Object> result = new HashMap<>();
        result.put("list", orders.subList(fromIndex, toIndex));
        result.put("total", orders.size());
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return result;
    }
}
