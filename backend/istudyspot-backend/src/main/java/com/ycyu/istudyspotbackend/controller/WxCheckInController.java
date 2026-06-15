package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.CheckInRecord;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.mapper.CheckInRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wx")
public class WxCheckInController {

    @Autowired
    private CheckInRecordMapper checkInRecordMapper;

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

            CheckInRecord record = new CheckInRecord();
            record.setUserId(userId);
            record.setReservationId(Long.valueOf(reservationIdStr));
            record.setSeatId(Long.valueOf(seatIdStr));
            checkInRecordMapper.insert(record);

            Map<String, Object> result = new HashMap<>();
            result.put("checkInRecordId", record.getId());
            result.put("checkInTime", record.getCheckInTime());
            result.put("reservationId", reservationIdStr);
            result.put("seatId", seatIdStr);
            return Result.success("签到成功", result);
        } catch (Exception e) {
            return Result.internalServerError(e.getMessage());
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

            CheckInRecord record = checkInRecordMapper.findById(checkInRecordId);
            if (record == null) {
                return Result.error("签到记录不存在");
            }
            if ("completed".equals(record.getStatus())) {
                return Result.error("已经签退");
            }

            long duration = java.time.temporal.ChronoUnit.MINUTES.between(
                    record.getCheckInTime(), java.time.LocalDateTime.now());
            checkInRecordMapper.checkout(checkInRecordId, (int) duration);

            Map<String, Object> result = new HashMap<>();
            result.put("checkOutTime", java.time.LocalDateTime.now());
            result.put("duration", (int) duration);
            return Result.success("签退成功", result);
        } catch (Exception e) {
            return Result.internalServerError(e.getMessage());
        }
    }

    @GetMapping("/checkin/records")
    public Result<Map<String, Object>> getCheckInRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {

        List<CheckInRecord> records;
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            records = checkInRecordMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            records = checkInRecordMapper.findByUserId(userId);
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int total = records.size();
        int fromIndex = Math.min((safePage - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        List<CheckInRecord> paged = fromIndex < total
                ? records.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", paged);
        result.put("total", total);
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return Result.success("success", result);
    }

    @GetMapping("/checkin/current")
    public Result<Map<String, Object>> getCurrentCheckInStatus(
            @RequestAttribute Long userId) {
        CheckInRecord record = checkInRecordMapper.findActiveByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        if (record != null) {
            result.put("isCheckedIn", true);
            result.put("checkInRecord", record);
        } else {
            result.put("isCheckedIn", false);
            result.put("checkInRecord", null);
        }
        return Result.success("success", result);
    }
}
