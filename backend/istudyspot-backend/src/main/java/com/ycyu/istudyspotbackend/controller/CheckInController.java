package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckInController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        try {
            String reservationIdStr = params.get("reservationId");
            if (reservationIdStr == null || reservationIdStr.isEmpty()) {
                return Result.error("预约ID不能为空");
            }
            Long reservationId = Long.valueOf(reservationIdStr);
            Map<String, Object> result = orderService.checkin(reservationId, "");
            return Result.success("签到成功", result);
        } catch (NumberFormatException e) {
            return Result.error("预约ID格式错误");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/checkout")
    public Result<Map<String, Object>> checkout(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        try {
            String orderIdStr = params.get("orderId");
            if (orderIdStr == null || orderIdStr.isEmpty()) {
                return Result.error("订单ID不能为空");
            }
            Long orderId = Long.valueOf(orderIdStr);
            Map<String, Object> result = orderService.checkout(orderId);
            return Result.success("签退成功", result);
        } catch (NumberFormatException e) {
            return Result.error("订单ID格式错误");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/checkin/records")
    public Result<Map<String, Object>> getCheckInRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        // TODO: 实现签到记录查询
        Map<String, Object> result = Map.of(
                "list", new Object(),
                "total", 0,
                "page", page,
                "pageSize", pageSize
        );
        return Result.success("success", result);
    }

    @GetMapping("/checkin/current")
    public Result<Map<String, Object>> getCurrentCheckInStatus(
            @RequestAttribute Long userId) {
        // TODO: 实现当前签到状态查询
        Map<String, Object> result = Map.of(
                "isCheckedIn", false,
                "checkInRecord", null
        );
        return Result.success("success", result);
    }
}