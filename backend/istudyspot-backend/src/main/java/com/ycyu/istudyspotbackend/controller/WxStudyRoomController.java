package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.dto.WxRuleDTO;
import com.ycyu.istudyspotbackend.dto.WxStudyRoomDTO;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Rule;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.RuleMapper;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wx/studyrooms")
public class WxStudyRoomController {

    @Autowired
    private StudyRoomService studyRoomService;

    @Autowired
    private RuleMapper ruleMapper;

    @GetMapping
    public Result<Map<String, Object>> getStudyRoomList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Map<String, Object> result = studyRoomService.getStudyRoomList(status, floor, keyword, page, pageSize);
        // 将 StudyRoom 列表转换为 WxStudyRoomDTO 列表
        Object listObj = result.get("list");
        if (listObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<StudyRoom> rooms = (List<StudyRoom>) listObj;
            result.put("list", rooms.stream().map(WxStudyRoomDTO::fromEntity).collect(Collectors.toList()));
        }
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<WxStudyRoomDTO> getStudyRoomDetail(@PathVariable Long id) {
        try {
            StudyRoom room = studyRoomService.getStudyRoomDetail(id);
            WxStudyRoomDTO dto = WxStudyRoomDTO.fromEntity(room);
            
            // 查询该自习室的规则
            List<Rule> rules = ruleMapper.findByStudyRoomId(id);
            List<WxRuleDTO> ruleDTOs = rules.stream()
                    .map(WxRuleDTO::fromEntity)
                    .collect(Collectors.toList());
            dto.setRules(ruleDTOs);
            
            return Result.success("success", dto);
        } catch (RuntimeException e) {
            return Result.notFound(e.getMessage());
        }
    }
}
