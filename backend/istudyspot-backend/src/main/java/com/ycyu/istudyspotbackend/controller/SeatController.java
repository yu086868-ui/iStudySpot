package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/studyrooms/{id}/seats")
    public Result<Map<String, Object>> getSeatMap(@PathVariable("id") Long roomId) {
        Map<String, Object> result = seatService.getSeatMap(roomId);
        return Result.success("获取成功", result);
    }

    @GetMapping("/seats/{id}")
    public Result<Seat> getSeatDetail(@PathVariable Long id) {
        Seat seat = seatService.getSeatDetail(id);
        return Result.success("获取成功", seat);
    }

    @GetMapping("/seats/{id}/timeline")
    public Result<Map<String, Object>> getSeatTimeline(
            @PathVariable Long id,
            @RequestParam String date) {
        Map<String, Object> result = seatService.getSeatTimeline(id, date);
        return Result.success("获取成功", result);
    }

    @PostMapping("/seats/calculate")
    public Result<Map<String, Object>> calculatePrice(@RequestBody Map<String, String> params) {
        Long seatId = Long.valueOf(params.get("seatId"));
        String startTime = params.get("startTime");
        String endTime = params.get("endTime");
        Map<String, Object> result = seatService.calculatePrice(seatId, startTime, endTime);
        return Result.success("计算成功", result);
    }
}