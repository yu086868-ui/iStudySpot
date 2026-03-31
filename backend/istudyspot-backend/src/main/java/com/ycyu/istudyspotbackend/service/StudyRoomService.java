package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import java.util.Map;

public interface StudyRoomService {
    Map<String, Object> getStudyRoomList(int page, int size);
    StudyRoom getStudyRoomDetail(Long id);
}