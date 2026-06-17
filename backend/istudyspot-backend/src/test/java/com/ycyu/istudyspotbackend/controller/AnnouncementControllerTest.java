package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnnouncementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private AnnouncementController announcementController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(announcementController).build();
    }

    @Test
    void testGetAnnouncementList() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("list", List.of());
        payload.put("total", 0);
        payload.put("page", 1);
        payload.put("pageSize", 20);
        when(announcementService.getAnnouncementList(null, null, 1, 20)).thenReturn(payload);

        mockMvc.perform(get("/api/announcements")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20));

        verify(announcementService).getAnnouncementList(null, null, 1, 20);
    }

    @Test
    void testGetAnnouncementDetail() throws Exception {
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("示例公告");
        announcement.setContent("这是一条公告内容");
        when(announcementService.getAnnouncementDetail(1L)).thenReturn(announcement);

        mockMvc.perform(get("/api/announcements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("示例公告"))
                .andExpect(jsonPath("$.data.content").value("这是一条公告内容"));

        verify(announcementService).getAnnouncementDetail(1L);
    }

    @Test
    void testGetAnnouncementDetailNotFound() throws Exception {
        when(announcementService.getAnnouncementDetail(99L)).thenThrow(new RuntimeException("announcement not found"));

        mockMvc.perform(get("/api/announcements/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("announcement not found"));

        verify(announcementService).getAnnouncementDetail(99L);
    }
}
