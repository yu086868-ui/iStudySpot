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
        Long reservationId = Long.valueOf(params.get("reservationId"));
        Long seatId = Long.valueOf(params.get("seatId"));
        Map<String, Object> result = orderService.checkin(reservationId, "");
        return Result.success("签到成功", result);
    }

    @PostMapping("/checkout")
    public Result<Map<String, Object>> checkout(
            @RequestBody Map<String, String> params,
            @RequestAttribute Long userId) {
        Long checkInRecordId = Long.valueOf(params.get("checkInRecordId"));
        Map<String, Object> result = orderService.checkout(checkInRecordId);
        return Result.success("签退成功", result);
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