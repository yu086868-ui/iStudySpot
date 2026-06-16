package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Announcement;
import com.ycyu.istudyspotbackend.mapper.AnnouncementMapper;
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
class WxAnnouncementControllerTest {

    @Mock
    private AnnouncementMapper announcementMapper;

    @InjectMocks
    private WxAnnouncementController wxAnnouncementController;

    private MockMvc mockMvc;

    private Announcement createAnnouncement(Long id, String title, String type) {
        Announcement a = new Announcement();
        a.setId(id);
        a.setTitle(title);
        a.setContent("Content");
        a.setType(type);
        a.setPriority("normal");
        a.setPublishTime(LocalDateTime.now());
        return a;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxAnnouncementController).build();
    }

    @Test
    void testGetAnnouncementList_All() throws Exception {
        List<Announcement> list = Arrays.asList(
                createAnnouncement(1L, "公告1", "notice"),
                createAnnouncement(2L, "公告2", "event"));
        when(announcementMapper.findAll()).thenReturn(list);

        mockMvc.perform(get("/api/wx/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void testGetAnnouncementList_ByType() throws Exception {
        List<Announcement> list = Arrays.asList(createAnnouncement(1L, "公告1", "notice"));
        when(announcementMapper.findByType(anyString())).thenReturn(list);

        mockMvc.perform(get("/api/wx/announcements")
                .param("type", "notice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(1));
    }

    @Test
    void testGetAnnouncementList_ByPriority() throws Exception {
        List<Announcement> list = Arrays.asList(createAnnouncement(1L, "公告1", "notice"));
        when(announcementMapper.findByPriority(anyString())).thenReturn(list);

        mockMvc.perform(get("/api/wx/announcements")
                .param("priority", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetAnnouncementDetail_Found() throws Exception {
        Announcement a = createAnnouncement(1L, "公告详情", "notice");
        when(announcementMapper.findById(anyLong())).thenReturn(a);

        mockMvc.perform(get("/api/wx/announcements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("公告详情"));
    }

    @Test
    void testGetAnnouncementDetail_NotFound() throws Exception {
        when(announcementMapper.findById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/wx/announcements/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}