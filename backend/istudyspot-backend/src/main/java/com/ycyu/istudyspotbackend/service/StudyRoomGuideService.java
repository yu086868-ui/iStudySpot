package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;

import java.util.List;

public interface StudyRoomGuideService {
    List<StudyRoomGuideSummary> getGuideList();
    StudyRoomGuideDetail getGuideDetail(Long studyRoomId);
}
