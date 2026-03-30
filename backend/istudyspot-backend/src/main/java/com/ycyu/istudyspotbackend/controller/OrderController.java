package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.dto.CheckinDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public Result<Map<String, Object>> createOrder(
            @RequestBody BookingDTO bookingDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.createOrder(
                userId,
                bookingDTO.getSeatId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime(),
                bookingDTO.getBookingType()
        );
        return Result.created(result);
    }

    @GetMapping("/users/me/orders")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestAttribute Long userId) {
        List<Order> orders = orderService.getOrderList(userId, status);
        Map<String, Object> result = Map.of("total", orders.size(), "list", orders);
        return Result.success("获取成功", result);
    }

    @GetMapping("/orders/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success("获取成功", order);
    }

    @PutMapping("/orders/{id}/cancel")
    public Result<Map<String, Object>> cancelOrder(@PathVariable Long id) {
        Map<String, Object> result = orderService.cancelOrder(id);
        return Result.success("订单取消成功", result);
    }

    @PostMapping("/orders/{id}/checkin")
    public Result<Map<String, Object>> checkin(
            @PathVariable Long id,
            @RequestBody CheckinDTO checkinDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.checkin(id, checkinDTO.getCheckinCode());
        return Result.success("签到成功", result);
    }

    @PostMapping("/orders/{id}/checkout")
    public Result<Map<String, Object>> checkout(@PathVariable Long id) {
        Map<String, Object> result = orderService.checkout(id);
        return Result.success("签退成功", result);
    }

    @PostMapping("/orders/{id}/renew")
    public Result<Map<String, Object>> renewOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        java.time.LocalDateTime newEndTime = java.time.LocalDateTime.parse(params.get("newEndTime"));
        Map<String, Object> result = orderService.renew(id, newEndTime);
        return Result.success("续费成功", result);
    }
}