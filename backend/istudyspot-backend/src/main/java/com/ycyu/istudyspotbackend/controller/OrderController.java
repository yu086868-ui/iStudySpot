package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    public Result<List<Order>> getOrderList(@RequestParam(required = false) Integer status,
                                            @RequestAttribute Long userId) {
        List<Order> orders = orderService.getOrderList(userId, status);
        return Result.success(orders);
    }

    @GetMapping("/detail/{orderId}")
    public Result<Order> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetail(orderId);
        return Result.success(order);
    }

    @PostMapping("/pay/{orderId}")
    public Result<Map<String, Object>> payOrder(@PathVariable Long orderId,
                                                @RequestBody Map<String, Integer> params) {
        Integer payType = params.get("payType");
        Map<String, Object> result = orderService.payOrder(orderId, payType);
        return Result.success(result);
    }

    @PostMapping("/cancel/{orderId}")
    public Result<Map<String, Object>> cancelOrder(@PathVariable Long orderId,
                                                   @RequestBody Map<String, String> params) {
        String reason = params.get("reason");
        Map<String, Object> result = orderService.cancelOrder(orderId, reason);
        return Result.success(result);
    }

    @PostMapping("/checkin/{orderId}")
    public Result<Map<String, Object>> checkin(@PathVariable Long orderId) {
        Map<String, Object> result = orderService.checkin(orderId);
        return Result.success(result);
    }

    @PostMapping("/checkout/{orderId}")
    public Result<Map<String, Object>> checkout(@PathVariable Long orderId) {
        Map<String, Object> result = orderService.checkout(orderId);
        return Result.success(result);
    }
}