package com.ycyu.istudyspotbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @InjectMocks
    private StatisticsController statisticsController;

    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.data.totalBookings").value(0));
    }

    @Test
    void testGetStatistics_WithDateRange() throws Exception {
        mockMvc.perform(get("/api/studyrooms/1/statistics")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studyRoomId").value(1))
                .andExpect(jsonPath("$.data.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.data.endDate").value("2026-06-01"));
    }
}