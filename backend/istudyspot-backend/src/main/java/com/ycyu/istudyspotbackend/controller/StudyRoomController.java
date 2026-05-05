package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/studyrooms")
public class StudyRoomController {

    @Autowired
    private StudyRoomService studyRoomService;

    @GetMapping
    public Result<Map<String, Object>> getStudyRoomList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> result = studyRoomService.getStudyRoomList(status, floor, keyword, page, pageSize);
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<StudyRoom> getStudyRoomDetail(@PathVariable Long id) {
        try {
            StudyRoom room = studyRoomService.getStudyRoomDetail(id);
            return Result.success("success", room);
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }
}