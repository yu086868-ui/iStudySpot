package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/studyrooms")
public class StatisticsController {

    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> getStatistics(
            @PathVariable Long id,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> statistics = Map.of(
                "studyRoomId", id,
                "startDate", startDate != null ? startDate : "",
                "endDate", endDate != null ? endDate : "",
                "totalBookings", 0,
                "totalHours", 0,
                "averageOccupancy", 0.0,
                "peakHours", Map.of(),
                "dailyBreakdown", Map.of()
        );
        return Result.success("success", statistics);
    }
}
