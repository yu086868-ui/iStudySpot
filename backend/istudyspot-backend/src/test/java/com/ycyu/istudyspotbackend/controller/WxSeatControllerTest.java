package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.SeatLayoutResponse;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxSeatControllerTest {

    @Mock
    private SeatService seatService;

    @InjectMocks
    private WxSeatController wxSeatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxSeatController).build();
    }

    @Test
    void testGetSeatList() throws Exception {
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setRoomId(1L);
        seat.setSeatNumber("A-1");
        seat.setRowNum(1);
        seat.setColNum(1);
        seat.setStatus("available");
        seat.setSeatType(1);
        seat.setPricePerHour(new BigDecimal("10.00"));
        seat.setCreateTime(LocalDateTime.now());
        seat.setUpdateTime(LocalDateTime.now());

        when(seatService.getSeatList(anyLong(), any(), any(), any(), any())).thenReturn(List.of(seat));

        mockMvc.perform(get("/api/wx/studyrooms/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].seatNumber").value("A-1"));
    }

    @Test
    void testGetSeatLayout() throws Exception {
        SeatLayoutResponse layout = new SeatLayoutResponse();
        layout.setStudyRoomId(1L);
        layout.setStudyRoomName("自习室A");
        layout.setItems(List.of());

        when(seatService.getSeatLayout(anyLong())).thenReturn(layout);

        mockMvc.perform(get("/api/wx/studyrooms/1/seat-layout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.studyRoomName").value("自习室A"));
    }

    @Test
    void testGetSeatLayout_WithItems() throws Exception {
        SeatLayoutResponse layout = new SeatLayoutResponse();
        layout.setStudyRoomId(1L);
        layout.setStudyRoomName("自习室A");
        layout.setCellSize(40);
        layout.setLayoutMode("grid");

        com.ycyu.istudyspotbackend.entity.SeatLayoutItem item = new com.ycyu.istudyspotbackend.entity.SeatLayoutItem();
        item.setId(1L);
        item.setLabel("A-1");
        item.setRowNum(1);
        item.setColNum(1);
        item.setItemType("seat");
        layout.setItems(List.of(item));

        layout.setLegend(Map.of("available", "可选"));

        when(seatService.getSeatLayout(anyLong())).thenReturn(layout);

        mockMvc.perform(get("/api/wx/studyrooms/1/seat-layout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].label").value("A-1"))
                .andExpect(jsonPath("$.data.legend.available").value("可选"));
    }

    @Test
    void testGetSeatDetail() throws Exception {
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setRoomId(1L);
        seat.setSeatNumber("A-1");
        seat.setRowNum(1);
        seat.setColNum(1);
        seat.setStatus("available");
        seat.setSeatType(1);
        seat.setPricePerHour(new BigDecimal("10.00"));
        seat.setCreateTime(LocalDateTime.now());
        seat.setUpdateTime(LocalDateTime.now());

        when(seatService.getSeatDetail(anyLong())).thenReturn(seat);

        mockMvc.perform(get("/api/wx/seats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.seatNumber").value("A-1"));
    }
}