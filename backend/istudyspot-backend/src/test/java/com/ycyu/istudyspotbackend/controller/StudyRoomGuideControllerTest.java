package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.StudyRoomGuideDetail;
import com.ycyu.istudyspotbackend.entity.StudyRoomGuideSummary;
import com.ycyu.istudyspotbackend.service.StudyRoomGuideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudyRoomGuideControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudyRoomGuideService studyRoomGuideService;

    @InjectMocks
    private StudyRoomGuideController studyRoomGuideController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studyRoomGuideController).build();
    }

    @Test
    void testGetGuideList() throws Exception {
        StudyRoomGuideSummary summary = new StudyRoomGuideSummary();
        summary.setStudyRoomId(1L);
        summary.setStudyRoomName("五道口店");
        summary.setAddress("北京市海淀区");

        when(studyRoomGuideService.getGuideList()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/studyrooms/guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].studyRoomId").value(1))
                .andExpect(jsonPath("$.data[0].studyRoomName").value("五道口店"));

        verify(studyRoomGuideService, times(1)).getGuideList();
    }

    @Test
    void testGetGuideDetail() throws Exception {
        StudyRoomGuideDetail detail = new StudyRoomGuideDetail();
        detail.setStudyRoomId(1L);
        detail.setStudyRoomName("五道口店");
        detail.setContactInfo("电话：010-62550101");

        when(studyRoomGuideService.getGuideDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/studyrooms/guides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studyRoomId").value(1))
                .andExpect(jsonPath("$.data.contactInfo").value("电话：010-62550101"));

        verify(studyRoomGuideService, times(1)).getGuideDetail(1L);
    }

    @Test
    void testGetGuideDetailNotFound() throws Exception {
        when(studyRoomGuideService.getGuideDetail(99L)).thenThrow(new RuntimeException("场馆导览不存在"));

        mockMvc.perform(get("/api/studyrooms/guides/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("场馆导览不存在"));

        verify(studyRoomGuideService, times(1)).getGuideDetail(99L);
    }
}
