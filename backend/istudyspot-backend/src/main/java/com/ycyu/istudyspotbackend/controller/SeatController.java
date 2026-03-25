package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/status")
    public Result<List<Seat>> getSeatStatus(@RequestParam Long roomId) {
        List<Seat> seats = seatService.getSeatStatus(roomId);
        return Result.success(seats);
    }

    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculatePrice(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        String startTime = params.get("startTime").toString();
        String endTime = params.get("endTime").toString();
        Map<String, Object> result = seatService.calculatePrice(seatId, startTime, endTime);
        return Result.success(result);
    }

    @PostMapping("/book")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params,
                                                   @RequestAttribute Long userId) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        String startTime = params.get("startTime").toString();
        String endTime = params.get("endTime").toString();
        Map<String, Object> result = seatService.createOrder(userId, seatId, startTime, endTime);
        return Result.success(result);
    }
}