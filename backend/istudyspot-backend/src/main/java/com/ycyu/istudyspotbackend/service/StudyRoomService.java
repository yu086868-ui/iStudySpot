package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.StudyRoom;

import java.util.Map;

public interface StudyRoomService {
    Map<String, Object> getStudyRoomList(String status, Integer floor, String keyword, int page, int pageSize);
    StudyRoom getStudyRoomDetail(Long id);
}