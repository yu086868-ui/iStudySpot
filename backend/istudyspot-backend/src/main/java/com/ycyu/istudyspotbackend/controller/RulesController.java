package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RulesController {

    @GetMapping
    public Result<java.util.List<java.util.Map<String, Object>>> getRulesList(
            @RequestParam(required = false) String studyRoomId,
            @RequestParam(required = false) String category) {
        // TODO: 实现规则列表查询
        java.util.List<java.util.Map<String, Object>> rules = new java.util.ArrayList<>();
        java.util.Map<String, Object> rule = new java.util.HashMap<>();
        rule.put("id", "1");
        rule.put("studyRoomId", studyRoomId);
        rule.put("category", category != null ? category : "booking");
        rule.put("title", "预约规则");
        rule.put("content", "预约规则内容");
        rule.put("priority", 1);
        rule.put("createdAt", "2024-01-01T00:00:00Z");
        rule.put("updatedAt", "2024-01-01T00:00:00Z");
        rules.add(rule);
        return Result.success("success", rules);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getRuleDetail(@PathVariable Long id) {
        // TODO: 实现规则详情查询
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", id.toString());
        result.put("studyRoomId", null);
        result.put("category", "booking");
        result.put("title", "预约规则");
        result.put("content", "预约规则内容");
        result.put("priority", 1);
        result.put("createdAt", "2024-01-01T00:00:00Z");
        result.put("updatedAt", "2024-01-01T00:00:00Z");
        return Result.success("success", result);
    }
}