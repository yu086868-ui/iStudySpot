package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Rule;
import com.ycyu.istudyspotbackend.mapper.RuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxRulesControllerTest {

    @Mock
    private RuleMapper ruleMapper;

    @InjectMocks
    private WxRulesController wxRulesController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxRulesController).build();
    }

    private Rule createRule(Long id, String title, String category) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setTitle(title);
        rule.setContent("Rule content");
        rule.setCategory(category);
        rule.setPriority(1);
        rule.setStudyRoomId(1L);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return rule;
    }

    @Test
    void testGetRulesList_All() throws Exception {
        List<Rule> rules = Arrays.asList(createRule(1L, "预约规则", "booking"), createRule(2L, "签到规则", "checkin"));
        when(ruleMapper.findAll()).thenReturn(rules);

        mockMvc.perform(get("/api/wx/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetRulesList_ByStudyRoomId() throws Exception {
        List<Rule> rules = Arrays.asList(createRule(1L, "规则1", "booking"));
        when(ruleMapper.findByStudyRoomId(anyLong())).thenReturn(rules);

        mockMvc.perform(get("/api/wx/rules")
                .param("studyRoomId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void testGetRulesList_ByCategory() throws Exception {
        List<Rule> rules = Arrays.asList(createRule(1L, "规则1", "booking"));
        when(ruleMapper.findGeneralByCategory(anyString())).thenReturn(rules);

        mockMvc.perform(get("/api/wx/rules")
                .param("category", "booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetRulesList_ByStudyRoomIdAndCategory() throws Exception {
        List<Rule> rules = Arrays.asList(createRule(1L, "规则1", "booking"));
        when(ruleMapper.findByStudyRoomId(anyLong())).thenReturn(rules);

        mockMvc.perform(get("/api/wx/rules")
                .param("studyRoomId", "1")
                .param("category", "booking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetRuleDetail_Found() throws Exception {
        Rule rule = createRule(1L, "规则详情", "booking");
        when(ruleMapper.findById(anyLong())).thenReturn(rule);

        mockMvc.perform(get("/api/wx/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("规则详情"));
    }

    @Test
    void testGetRuleDetail_NotFound() throws Exception {
        when(ruleMapper.findById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/wx/rules/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}