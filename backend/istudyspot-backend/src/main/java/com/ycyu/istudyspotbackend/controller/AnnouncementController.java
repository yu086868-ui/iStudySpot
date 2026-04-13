package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @GetMapping
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        // TODO: 实现公告列表查询
        Map<String, Object> result = Map.of(
                "list", new Object(),
                "total", 0,
                "page", page,
                "pageSize", pageSize
        );
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getAnnouncementDetail(@PathVariable Long id) {
        // TODO: 实现公告详情查询
        Map<String, Object> result = Map.of(
                "id", id.toString(),
                "title", "示例公告",
                "content", "这是一个示例公告",
                "type", "notice",
                "priority", "medium",
                "publishTime", "2024-01-01T00:00:00Z",
                "expireTime", "2024-01-31T23:59:59Z",
                "author", "管理员",
                "status", "published"
        );
        return Result.success("success", result);
    }
}