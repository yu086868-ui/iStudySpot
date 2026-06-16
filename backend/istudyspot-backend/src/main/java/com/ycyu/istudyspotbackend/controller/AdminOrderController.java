package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AdminAccessService;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AdminAccessService adminAccessService;

    @GetMapping
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        try {
            adminAccessService.checkAdmin(userId);
            return Result.success("success", orderService.getAdminOrderList(keyword, status, page, pageSize));
        } catch (RuntimeException e) {
            return Result.forbidden(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            adminAccessService.checkAdmin(userId);
            return Result.success("success", orderService.getOrderDetail(id));
        } catch (RuntimeException e) {
            return Result.forbidden(e.getMessage());
        }
    }
}
