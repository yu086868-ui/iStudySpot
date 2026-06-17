package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AdminAccessService;
import com.ycyu.istudyspotbackend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private AdminAccessService adminAccessService;

    @GetMapping
    public Result<Map<String, Object>> getAnnouncementList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        try {
            adminAccessService.checkAdmin(userId);
            return Result.success("success", announcementService.getAdminAnnouncementList(type, priority, status, page, pageSize));
        } catch (RuntimeException e) {
            return Result.forbidden(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementDetail(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            adminAccessService.checkAdmin(userId);
            return Result.success("success", announcementService.getAnnouncementDetail(id));
        } catch (IllegalArgumentException e) {
            return Result.notFound(e.getMessage());
        } catch (RuntimeException e) {
            return Result.forbidden(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id, @RequestAttribute Long userId) {
        try {
            adminAccessService.checkAdmin(userId);
            announcementService.deleteAnnouncement(id);
            return Result.success("删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.notFound(e.getMessage());
        } catch (RuntimeException e) {
            return Result.forbidden(e.getMessage());
        }
    }
}
