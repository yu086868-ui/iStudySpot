package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Achievement;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.UserAchievement;
import com.ycyu.istudyspotbackend.mapper.AchievementMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AchievementControllerTest {

    @Mock
    private AchievementMapper achievementMapper;

    @Mock
    private com.ycyu.istudyspotbackend.mapper.OrderMapper orderMapper;

    @InjectMocks
    private AchievementController achievementController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(achievementController).build();
    }

    @Test
    void testGetAchievements_NoAchievements() throws Exception {
        when(achievementMapper.findAll()).thenReturn(Collections.emptyList());
        when(achievementMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAchievements_WithAchievements() throws Exception {
        Achievement ach1 = new Achievement();
        ach1.setCode("early_bird");
        ach1.setName("早起的鸟儿");
        ach1.setDescription("早起打卡3天");
        ach1.setIcon("icon1");
        ach1.setCategory("habit");

        Achievement ach2 = new Achievement();
        ach2.setCode("night_owl");
        ach2.setName("夜猫子");
        ach2.setDescription("夜间学习3天");
        ach2.setIcon("icon2");
        ach2.setCategory("habit");

        List<Achievement> allAchievements = Arrays.asList(ach1, ach2);

        when(achievementMapper.findAll()).thenReturn(allAchievements);
        when(achievementMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("early_bird"))
                .andExpect(jsonPath("$.data[0].isUnlocked").value(false))
                .andExpect(jsonPath("$.data[1].code").value("night_owl"));
    }

    @Test
    void testGetAchievements_WithUnlockedAchievements() throws Exception {
        Achievement ach = new Achievement();
        ach.setCode("early_bird");
        ach.setName("早起的鸟儿");
        ach.setDescription("早起打卡3天");
        ach.setIcon("icon1");
        ach.setCategory("habit");

        UserAchievement ua = new UserAchievement();
        ua.setAchievementCode("early_bird");
        ua.setUserId(1L);
        ua.setUnlockedAt(LocalDateTime.now());

        when(achievementMapper.findAll()).thenReturn(List.of(ach));
        when(achievementMapper.findByUserId(anyLong())).thenReturn(List.of(ua));
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isUnlocked").value(true));
    }

    @Test
    void testGetAchievements_WithCheckinRecords() throws Exception {
        Achievement ach = new Achievement();
        ach.setCode("early_bird");
        ach.setName("早起的鸟儿");
        ach.setDescription("早起打卡3天");
        ach.setIcon("icon1");
        ach.setCategory("habit");

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order();
        order.setActualStartTime(now.minusHours(2));
        order.setActualEndTime(now);
        order.setPlanStartTime(now.minusHours(2));
        order.setPlanEndTime(now);
        order.setSeatPosition("A-1");

        when(achievementMapper.findAll()).thenReturn(List.of(ach));
        when(achievementMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(List.of(order));
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetAchievements_WithEarlyBirdAndNightOwl() throws Exception {
        Achievement ach = new Achievement();
        ach.setCode("early_bird");
        ach.setName("早起的鸟儿");
        ach.setDescription("早起打卡3天");
        ach.setIcon("icon1");
        ach.setCategory("habit");

        LocalDateTime now = LocalDateTime.now();
        // Early bird: start at 7am
        Order earlyBird = new Order();
        earlyBird.setActualStartTime(now.withHour(7).withMinute(30));
        earlyBird.setActualEndTime(now.withHour(10).withMinute(0));
        earlyBird.setPlanStartTime(now.withHour(7).withMinute(30));
        earlyBird.setPlanEndTime(now.withHour(10).withMinute(0));
        earlyBird.setSeatPosition("A-1");

        // Night owl: end at 22:00
        Order nightOwl = new Order();
        nightOwl.setActualStartTime(now.withHour(19).withMinute(0));
        nightOwl.setActualEndTime(now.withHour(22).withMinute(0));
        nightOwl.setPlanStartTime(now.withHour(19).withMinute(0));
        nightOwl.setPlanEndTime(now.withHour(22).withMinute(0));
        nightOwl.setSeatPosition("A-1");

        when(achievementMapper.findAll()).thenReturn(List.of(ach));
        when(achievementMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(List.of(earlyBird, nightOwl));
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetAchievements_WithMultipleAchievements() throws Exception {
        Achievement ach1 = new Achievement();
        ach1.setCode("study_master");
        ach1.setName("学霸");
        ach1.setDescription("学习100小时");
        ach1.setIcon("icon3");
        ach1.setCategory("study");

        Achievement ach2 = new Achievement();
        ach2.setCode("streak_king");
        ach2.setName("连续打卡王");
        ach2.setDescription("连续7天");
        ach2.setIcon("icon4");
        ach2.setCategory("habit");

        LocalDateTime now = LocalDateTime.now();
        // Create 100+ hours of study across multiple days
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Order order = new Order();
            order.setActualStartTime(now.minusDays(i).withHour(8));
            order.setActualEndTime(now.minusDays(i).withHour(22));
            order.setPlanStartTime(now.minusDays(i).withHour(8));
            order.setPlanEndTime(now.minusDays(i).withHour(22));
            order.setSeatPosition("A-" + (i + 1));
            orders.add(order);
        }

        when(achievementMapper.findAll()).thenReturn(List.of(ach1, ach2));
        when(achievementMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(orderMapper.findCheckinRecordsByUserId(anyLong())).thenReturn(orders);
        when(orderMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/achievements")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}