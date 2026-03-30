package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudyRoomServiceImpl implements StudyRoomService {

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    @Override
    public Map<String, Object> getStudyRoomList(int page, int size) {
        List<StudyRoom> list = studyRoomMapper.findAll();
        int total = studyRoomMapper.count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
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
}