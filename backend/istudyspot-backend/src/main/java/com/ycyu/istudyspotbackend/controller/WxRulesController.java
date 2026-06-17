package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Rule;
import com.ycyu.istudyspotbackend.mapper.RuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wx/rules")
public class WxRulesController {

    @Autowired
    private RuleMapper ruleMapper;

    @GetMapping
    public Result<List<Rule>> getRulesList(
            @RequestParam(required = false) String studyRoomId,
            @RequestParam(required = false) String category) {
        List<Rule> rules;
        if (studyRoomId != null && !studyRoomId.isEmpty()) {
            Long roomLongId = Long.valueOf(studyRoomId);
            if (category != null && !category.isEmpty()) {
                rules = ruleMapper.findByStudyRoomId(roomLongId);
                rules = rules.stream().filter(r -> category.equals(r.getCategory())).toList();
            } else {
                rules = ruleMapper.findByStudyRoomId(roomLongId);
            }
        } else {
            if (category != null && !category.isEmpty()) {
                rules = ruleMapper.findGeneralByCategory(category);
            } else {
                rules = ruleMapper.findAll();
            }
        }
        return Result.success("success", rules);
    }

    @GetMapping("/{id}")
    public Result<Rule> getRuleDetail(@PathVariable Long id) {
        Rule rule = ruleMapper.findById(id);
        if (rule == null) {
            return Result.notFound("规则不存在");
        }
        return Result.success("success", rule);
    }
}
