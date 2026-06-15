package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    @Override
    public Map<String, Object> getStudyRoomList(String status, Integer floor, String keyword, int page, int pageSize) {
        List<StudyRoom> rooms;
        if (keyword != null && !keyword.trim().isEmpty()) {
            rooms = studyRoomMapper.search(keyword.trim());
        } else {
            rooms = studyRoomMapper.findAll();
        }

        List<StudyRoom> filteredRooms = filterByStatus(rooms, status);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int total = filteredRooms.size();
        int fromIndex = Math.min((safePage - 1) * safePageSize, total);
        int toIndex = Math.min(fromIndex + safePageSize, total);
        List<StudyRoom> pagedRooms = new ArrayList<>(filteredRooms.subList(fromIndex, toIndex));

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedRooms);
        result.put("total", total);
        result.put("page", safePage);
        result.put("pageSize", safePageSize);
        return result;
    }

    @Override
    public StudyRoom getStudyRoomDetail(Long id) {
        StudyRoom room = studyRoomMapper.findById(id);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }
        return room;
    }

    private List<StudyRoom> filterByStatus(List<StudyRoom> rooms, String status) {
        if (status == null || status.isBlank()) {
            return rooms;
        }
        Integer normalizedStatus = normalizeStatus(status);
        if (normalizedStatus == null) {
            return rooms;
        }
        return rooms.stream()
                .filter(room -> normalizedStatus.equals(room.getStatus()))
                .toList();
    }

    private Integer normalizeStatus(String status) {
        return switch (status.trim().toLowerCase()) {
            case "1", "open", "active", "available" -> 1;
            case "0", "closed", "inactive", "disabled" -> 0;
            default -> null;
        };
    }
}
