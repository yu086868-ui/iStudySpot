package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.service.SeatService;
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
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SeatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private SeatController seatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(seatController).build();
    }

    @Test
    void testGetSeatList() throws Exception {
        // 模拟座位列表
        List<Seat> seats = new ArrayList<>();
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setStatus("available");
        seat.setSeatType(1);
        seats.add(seat);

        doReturn(seats).when(seatService).getSeatList(1L, "available", "1", null, null);

        // 测试获取座位列表
        mockMvc.perform(get("/api/studyrooms/1/seats")
                .param("status", "available")
                .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].seatNumber").value("A1"));

        // 验证方法调用
        verify(seatService, times(1)).getSeatList(1L, "available", "1", null, null);
    }

    @Test
    void testGetSeatDetail() throws Exception {
        // 模拟座位详情
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("A1");
        seat.setStatus("available");
        seat.setSeatType(1);

        when(seatService.getSeatDetail(1L)).thenReturn(seat);

        // 测试获取座位详情
        mockMvc.perform(get("/api/seats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.seatNumber").value("A1"));

        // 验证方法调用
        verify(seatService, times(1)).getSeatDetail(1L);
    }
}
