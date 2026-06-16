package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wx/announcements")
public class WxAnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @GetMapping
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<Announcement> announcements;
        if (type != null && !type.isEmpty()) {
            announcements = announcementMapper.findByType(type);
        } else if (priority != null && !priority.isEmpty()) {
            announcements = announcementMapper.findByPriority(priority);
        } else {
            announcements = announcementMapper.findAll();
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int total = announcements.size();
        int fromIndex = Math.min((safePage - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        List<Announcement> paged = fromIndex < total
                ? announcements.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", paged);
        result.put("total", total);
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            return Result.notFound("公告不存在");
        }
        return Result.success("success", announcement);
    }
}
