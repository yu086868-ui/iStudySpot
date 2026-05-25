package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CheckInController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        List<Order> allRecords = orderMapper.findCheckinRecordsByUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        int totalMinutes = 0;
        int weekMinutes = 0;
        int monthMinutes = 0;
        int streakDays = 0;
        Map<String, Integer> seatUsageMap = new HashMap<>();
        Map<Integer, Integer> hourDistribution = new HashMap<>();

        LocalDate lastCheckinDate = null;
        List<Map<String, Object>> recordList = new ArrayList<>();

        for (Order order : allRecords) {
            LocalDateTime start = order.getActualStartTime() != null ? order.getActualStartTime() : order.getPlanStartTime();
            LocalDateTime end = order.getActualEndTime() != null ? order.getActualEndTime() : order.getPlanEndTime();
            if (start == null) start = order.getPlanStartTime();
            if (end == null) end = order.getPlanEndTime();
            if (start == null || end == null) continue;

            long minutes = ChronoUnit.MINUTES.between(start, end);
            totalMinutes += minutes;

            if (!start.isBefore(weekStart)) {
                weekMinutes += minutes;
            }
            if (!start.isBefore(monthStart)) {
                monthMinutes += minutes;
            }

            String seatKey = order.getSeatPosition() != null ? order.getSeatPosition() : "unknown";
            seatUsageMap.merge(seatKey, 1, Integer::sum);

            int hour = start.getHour();
            hourDistribution.merge(hour, 1, Integer::sum);

            LocalDate checkinDate = start.toLocalDate();
            if (lastCheckinDate == null || ChronoUnit.DAYS.between(lastCheckinDate, checkinDate) == 1) {
                streakDays++;
            } else if (ChronoUnit.DAYS.between(lastCheckinDate, checkinDate) > 1) {
                streakDays = 1;
            }
            lastCheckinDate = checkinDate;

            Map<String, Object> record = new HashMap<>();
            record.put("id", order.getId());
            record.put("seatPosition", order.getSeatPosition());
            record.put("studyRoomName", order.getStudyRoomName());
            record.put("startTime", start.format(formatter));
            record.put("endTime", end.format(formatter));
            record.put("duration", minutes);
            record.put("status", order.getStatus());
            recordList.add(record);
        }

        String favoriteSeat = seatUsageMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        String peakTime = hourDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> String.format("%02d:00-%02d:00", e.getKey(), e.getKey() + 1))
                .orElse("");

        double avgDuration = allRecords.isEmpty() ? 0.0 :
                (double) totalMinutes / allRecords.size() / 60.0;

        int startIdx = (page - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, recordList.size());
        List<Map<String, Object>> pagedRecords = startIdx < recordList.size()
                ? recordList.subList(startIdx, endIdx)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("records", pagedRecords);
        result.put("total", allRecords.size());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalHours", totalMinutes / 60);
        result.put("weekHours", weekMinutes / 60);
        result.put("monthHours", monthMinutes / 60);
        result.put("streak", streakDays);
        result.put("avgDuration", Math.round(avgDuration * 10.0) / 10.0);
        result.put("favoriteSeat", favoriteSeat);
        result.put("peakTime", peakTime);
        return Result.success("success", result);
    }

    @GetMapping("/checkin/current")
    public Result<Map<String, Object>> getCurrentCheckInStatus(
            @RequestAttribute Long userId) {
        Order currentOrder = orderMapper.findCurrentCheckinByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        if (currentOrder != null) {
            result.put("isCheckedIn", true);
            Map<String, Object> record = new HashMap<>();
            record.put("id", currentOrder.getId());
            record.put("seatPosition", currentOrder.getSeatPosition());
            record.put("studyRoomName", currentOrder.getStudyRoomName());
            record.put("startTime", currentOrder.getPlanStartTime() != null
                    ? currentOrder.getPlanStartTime().format(formatter) : null);
            record.put("endTime", currentOrder.getPlanEndTime() != null
                    ? currentOrder.getPlanEndTime().format(formatter) : null);
            record.put("status", currentOrder.getStatus());
            result.put("checkInRecord", record);
        } else {
            result.put("isCheckedIn", false);
            result.put("checkInRecord", null);
        }
        return Result.success("success", result);
    }
}
