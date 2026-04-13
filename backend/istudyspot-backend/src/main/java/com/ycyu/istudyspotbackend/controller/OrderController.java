package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<Map<String, Object>> createReservation(
            @RequestBody BookingDTO bookingDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.createOrder(
                userId,
                bookingDTO.getSeatId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime(),
                bookingDTO.getBookingType()
        );
        return Result.success("预约成功", result);
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.getOrderList(userId, status, startDate, endDate, page, pageSize);
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<Order> getReservationDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success("success", order);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("预约已取消", null);
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getReservationRules() {
        Map<String, Object> rules = Map.of(
                "maxAdvanceDays", 7,
                "maxDailyReservations", 2,
                "maxDurationHours", 4,
                "minDurationMinutes", 30,
                "cancellationDeadlineMinutes", 15,
                "noShowPenalty", 5
        );
        return Result.success("success", rules);
    }
}