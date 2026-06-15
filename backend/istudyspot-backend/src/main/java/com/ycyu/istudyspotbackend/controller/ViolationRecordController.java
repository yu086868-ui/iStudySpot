package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.ViolationRecord;
import com.ycyu.istudyspotbackend.mapper.ViolationRecordMapper;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/violations")
public class ViolationRecordController {

    @Autowired
    private ViolationRecordMapper violationRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public Result<Map<String, Object>> getViolationRecords(@RequestAttribute Long userId) {
        List<ViolationRecord> records = violationRecordMapper.findByUserId(userId);
        int totalCount = violationRecordMapper.countByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", totalCount);
        return Result.success("success", result);
    }

    @PostMapping("/{id}/appeal")
    public Result<Void> submitAppeal(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestAttribute Long userId) {
        ViolationRecord record = violationRecordMapper.findById(id);
        if (record == null) {
            return Result.error("违规记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return Result.error("无权操作此记录");
        }
        if (!"active".equals(record.getStatus())) {
            return Result.error("当前状态无法申诉");
        }
        String reason = body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("申诉理由不能为空");
        }
        violationRecordMapper.submitAppeal(id, reason);
        return Result.success("申诉已提交", null);
    }
}
