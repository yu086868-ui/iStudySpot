package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;
import com.ycyu.istudyspotbackend.service.StudyRoomGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/studyrooms/guides")
public class StudyRoomGuideController {

    @Autowired
    private StudyRoomGuideService studyRoomGuideService;

    @GetMapping
    public Result<List<StudyRoomGuideSummary>> getGuideList() {
        return Result.success("success", studyRoomGuideService.getGuideList());
    }

    @GetMapping("/{studyRoomId}")
    public Result<StudyRoomGuideDetail> getGuideDetail(@PathVariable Long studyRoomId) {
        try {
            return Result.success("success", studyRoomGuideService.getGuideDetail(studyRoomId));
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }
}
