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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = studyRoomService.getStudyRoomList(page, size);
        return Result.success("获取成功", result);
    }

    @GetMapping("/{id}")
    public Result<StudyRoom> getStudyRoomDetail(@PathVariable Long id) {
        try {
            StudyRoom room = studyRoomService.getStudyRoomDetail(id);
            return Result.success("获取成功", room);
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }
}