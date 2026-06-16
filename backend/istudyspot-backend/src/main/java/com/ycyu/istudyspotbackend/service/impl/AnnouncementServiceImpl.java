package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.mapper.AnnouncementMapper;
import com.ycyu.istudyspotbackend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public Map<String, Object> getAnnouncementList(String type, String priority, int page, int pageSize) {
        List<Announcement> source = announcementMapper.findAll().stream()
                .filter(item -> "published".equals(item.getStatus()))
                .filter(item -> type == null || type.isBlank() || Objects.equals(type, item.getType()))
                .filter(item -> priority == null || priority.isBlank() || Objects.equals(priority, item.getPriority()))
                .collect(Collectors.toList());
        return buildPagedResult(source, page, pageSize);
    }

    @Override
    public Map<String, Object> getAdminAnnouncementList(String type, String priority, String status, int page, int pageSize) {
        List<Announcement> source = announcementMapper.findAll().stream()
                .filter(item -> type == null || type.isBlank() || Objects.equals(type, item.getType()))
                .filter(item -> priority == null || priority.isBlank() || Objects.equals(priority, item.getPriority()))
                .filter(item -> status == null || status.isBlank() || Objects.equals(status, item.getStatus()))
                .collect(Collectors.toList());
        return buildPagedResult(source, page, pageSize);
    }

    @Override
    public Announcement getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        return announcement;
    }

    @Override
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementMapper.findById(id);
        if (announcement == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }

    private Map<String, Object> buildPagedResult(List<Announcement> source, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = Math.min((safePage - 1) * safePageSize, source.size());
        int toIndex = Math.min(fromIndex + safePageSize, source.size());

        Map<String, Object> result = new HashMap<>();
        result.put("list", source.subList(fromIndex, toIndex));
        result.put("total", source.size());
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return result;
    }
}
