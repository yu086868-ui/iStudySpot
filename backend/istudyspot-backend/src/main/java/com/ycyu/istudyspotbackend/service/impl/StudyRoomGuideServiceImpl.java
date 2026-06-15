package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;
import com.ycyu.istudyspotbackend.mapper.StudyRoomGuideMapper;
import com.ycyu.istudyspotbackend.service.StudyRoomGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyRoomGuideServiceImpl implements StudyRoomGuideService {

    @Autowired
    private StudyRoomGuideMapper studyRoomGuideMapper;

    @Override
    public List<StudyRoomGuideSummary> getGuideList() {
        return studyRoomGuideMapper.findGuideSummaries();
    }

    @Override
    public StudyRoomGuideDetail getGuideDetail(Long studyRoomId) {
        StudyRoomGuideDetail detail = studyRoomGuideMapper.findGuideDetailByStudyRoomId(studyRoomId);
        if (detail == null) {
            throw new RuntimeException("场馆导览不存在");
        }
        return detail;
    }
}
