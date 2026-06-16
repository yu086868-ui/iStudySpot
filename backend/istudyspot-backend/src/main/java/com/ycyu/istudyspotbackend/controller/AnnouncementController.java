package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success("success", announcementService.getAnnouncementList(type, priority, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementDetail(@PathVariable Long id) {
        try {
            return Result.success("success", announcementService.getAnnouncementDetail(id));
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }
}
