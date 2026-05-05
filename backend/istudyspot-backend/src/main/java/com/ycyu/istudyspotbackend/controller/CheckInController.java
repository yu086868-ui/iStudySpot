package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ArrayList;

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
            String seatIdStr = params.get("seatId");
            if (reservationIdStr == null || reservationIdStr.isEmpty()) {
                return Result.error("预约ID不能为空");
            }
            if (seatIdStr == null || seatIdStr.isEmpty()) {
                return Result.error("座位ID不能为空");
            }
            Long reservationId = Long.valueOf(reservationIdStr);
            Map<String, Object> result = orderService.checkin(reservationId, seatIdStr);
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
            String checkInRecordIdStr = params.get("checkInRecordId");
            if (checkInRecordIdStr == null || checkInRecordIdStr.isEmpty()) {
                return Result.error("签到记录ID不能为空");
            }
            Long checkInRecordId = Long.valueOf(checkInRecordIdStr);
            Map<String, Object> result = orderService.checkout(checkInRecordId);
            return Result.success("签退成功", result);
        } catch (NumberFormatException e) {
            return Result.error("签到记录ID格式错误");
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
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return Result.success("success", result);
    }

    @GetMapping("/checkin/current")
    public Result<Map<String, Object>> getCurrentCheckInStatus(
            @RequestAttribute Long userId) {
        // TODO: 实现当前签到状态查询
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("isCheckedIn", false);
        result.put("checkInRecord", null);
        return Result.success("success", result);
    }
}