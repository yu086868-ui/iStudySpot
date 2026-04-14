package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RulesController {

    @GetMapping
    public Result<Map<String, Object>> getRulesList(
            @RequestParam(required = false) String studyRoomId,
            @RequestParam(required = false) String category) {
        // TODO: 实现规则列表查询
        Map<String, Object> result = Map.of(
                "id", "1",
                "studyRoomId", studyRoomId,
                "category", category != null ? category : "booking",
                "title", "预约规则",
                "content", "预约规则内容",
                "priority", 1,
                "createdAt", "2024-01-01T00:00:00Z",
                "updatedAt", "2024-01-01T00:00:00Z"
        );
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRuleDetail(@PathVariable Long id) {
        // TODO: 实现规则详情查询
        Map<String, Object> result = Map.of(
                "id", id.toString(),
                "studyRoomId", null,
                "category", "booking",
                "title", "预约规则",
                "content", "预约规则内容",
                "priority", 1,
                "createdAt", "2024-01-01T00:00:00Z",
                "updatedAt", "2024-01-01T00:00:00Z"
        );
        return Result.success("success", result);
    }
}