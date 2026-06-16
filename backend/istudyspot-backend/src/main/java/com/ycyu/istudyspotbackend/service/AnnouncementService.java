package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Announcement;

import java.util.Map;

public interface AnnouncementService {
    Map<String, Object> getAnnouncementList(String type, String priority, int page, int pageSize);

    Map<String, Object> getAdminAnnouncementList(String type, String priority, String status, int page, int pageSize);

    Announcement getAnnouncementDetail(Long id);

    void deleteAnnouncement(Long id);
}
