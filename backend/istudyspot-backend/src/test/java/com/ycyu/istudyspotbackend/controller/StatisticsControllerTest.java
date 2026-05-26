package com.ycyu.istudyspotbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class StatisticsControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private StatisticsController statisticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();
    }

    @Test
    void testGetStatistics() throws Exception {
        mockMvc.perform(get("/api/studyrooms/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studyRoomId").value(1))
                .andExpect(jsonPath("$.data.totalBookings").value(0))
                .andExpect(jsonPath("$.data.totalHours").value(0))
                .andExpect(jsonPath("$.data.averageOccupancy").value(0.0));
    }

    @Test
    void testGetStatisticsWithDateParams() throws Exception {
        mockMvc.perform(get("/api/studyrooms/2/statistics")
                .param("startDate", "2026-05-01")
                .param("endDate", "2026-05-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studyRoomId").value(2))
                .andExpect(jsonPath("$.data.startDate").value("2026-05-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-05-25"));
    }

    @Test
    void testGetStatisticsWithOnlyStartDate() throws Exception {
        mockMvc.perform(get("/api/studyrooms/1/statistics")
                .param("startDate", "2026-05-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startDate").value("2026-05-01"))
                .andExpect(jsonPath("$.data.endDate").value(""));
    }

    @Test
    void testGetStatisticsWithOnlyEndDate() throws Exception {
        mockMvc.perform(get("/api/studyrooms/1/statistics")
                .param("endDate", "2026-05-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.startDate").value(""))
                .andExpect(jsonPath("$.data.endDate").value("2026-05-25"));
    }

    @Test
    void testGetStatisticsWithNoParams() throws Exception {
        mockMvc.perform(get("/api/studyrooms/3/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studyRoomId").value(3))
                .andExpect(jsonPath("$.data.startDate").value(""))
                .andExpect(jsonPath("$.data.endDate").value(""));
    }
}
