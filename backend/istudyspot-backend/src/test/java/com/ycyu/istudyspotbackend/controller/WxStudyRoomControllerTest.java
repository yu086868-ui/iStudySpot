package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Rule;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.RuleMapper;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxStudyRoomControllerTest {

    @Mock
    private StudyRoomService studyRoomService;

    @Mock
    private RuleMapper ruleMapper;

    @InjectMocks
    private WxStudyRoomController wxStudyRoomController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxStudyRoomController).build();
    }

    @Test
    void testGetStudyRoomList() throws Exception {
        StudyRoom room = new StudyRoom();
        room.setId(1L);
        room.setName("自习室A");
        room.setAddress("一楼101");
        room.setImageUrl("img.jpg");
        room.setStatus(1);
        room.setOpenTime(LocalTime.of(8, 0));
        room.setCloseTime(LocalTime.of(22, 0));
        room.setFloor(1);
        room.setCapacity(50);
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("list", List.of(room));
        result.put("total", 1);

        when(studyRoomService.getStudyRoomList(any(), any(), any(), anyInt(), anyInt())).thenReturn(result);

        mockMvc.perform(get("/api/wx/studyrooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testGetStudyRoomDetail() throws Exception {
        StudyRoom room = new StudyRoom();
        room.setId(1L);
        room.setName("自习室A");
        room.setAddress("一楼101");
        room.setImageUrl("img.jpg");
        room.setStatus(1);
        room.setOpenTime(LocalTime.of(8, 0));
        room.setCloseTime(LocalTime.of(22, 0));
        room.setFloor(1);
        room.setCapacity(50);
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());

        Rule rule = new Rule();
        rule.setId(1L);
        rule.setTitle("规则1");
        rule.setContent("内容");
        rule.setCategory("booking");
        rule.setPriority(1);
        rule.setStudyRoomId(1L);
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());

        when(studyRoomService.getStudyRoomDetail(anyLong())).thenReturn(room);
        when(ruleMapper.findByStudyRoomId(anyLong())).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/wx/studyrooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("自习室A"))
                .andExpect(jsonPath("$.data.rules.length()").value(1));
    }

    @Test
    void testGetStudyRoomDetail_NotFound() throws Exception {
        when(studyRoomService.getStudyRoomDetail(anyLong())).thenThrow(new RuntimeException("自习室不存在"));

        mockMvc.perform(get("/api/wx/studyrooms/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}