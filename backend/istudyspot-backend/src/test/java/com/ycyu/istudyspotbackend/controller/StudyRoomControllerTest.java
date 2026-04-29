package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StudyRoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudyRoomService studyRoomService;

    @InjectMocks
    private StudyRoomController studyRoomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studyRoomController).build();
    }

    @Test
    void testGetStudyRoomList() throws Exception {
        // 模拟自习室列表
        List<StudyRoom> studyRooms = new ArrayList<>();
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("自习室1");
        studyRooms.add(studyRoom);

        Map<String, Object> result = new HashMap<>();
        result.put("list", studyRooms);
        result.put("total", 1);
        result.put("page", 1);
        result.put("pageSize", 20);

        doReturn(result).when(studyRoomService).getStudyRoomList(null, null, null, 1, 20);

        // 测试获取自习室列表
        mockMvc.perform(get("/api/studyrooms")
                .param("page", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证方法调用
        verify(studyRoomService, times(1)).getStudyRoomList(null, null, null, 1, 20);
    }

    @Test
    void testGetStudyRoomDetail() throws Exception {
        // 模拟自习室详情
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("自习室1");
        studyRoom.setStatus(1);

        when(studyRoomService.getStudyRoomDetail(1L)).thenReturn(studyRoom);

        // 测试获取自习室详情
        mockMvc.perform(get("/api/studyrooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("自习室1"));

        // 验证方法调用
        verify(studyRoomService, times(1)).getStudyRoomDetail(1L);
    }

    @Test
    void testGetStudyRoomDetailNotFound() throws Exception {
        // 模拟自习室不存在
        when(studyRoomService.getStudyRoomDetail(1L)).thenThrow(new RuntimeException("自习室不存在"));

        // 测试获取自习室详情
        mockMvc.perform(get("/api/studyrooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("自习室不存在"));

        // 验证方法调用
        verify(studyRoomService, times(1)).getStudyRoomDetail(1L);
    }
}
