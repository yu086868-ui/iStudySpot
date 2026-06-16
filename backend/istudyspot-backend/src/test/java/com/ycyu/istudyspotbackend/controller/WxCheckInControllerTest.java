package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.CheckInRecord;
import com.ycyu.istudyspotbackend.mapper.CheckInRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WxCheckInControllerTest {

    @Mock
    private CheckInRecordMapper checkInRecordMapper;

    @InjectMocks
    private WxCheckInController wxCheckInController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wxCheckInController).build();
    }

    @Test
    void testCheckin_Success() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("reservationId", "1");
        params.put("seatId", "1");

        doAnswer(invocation -> {
            CheckInRecord r = invocation.getArgument(0);
            r.setId(1L);
            return null;
        }).when(checkInRecordMapper).insert(any(CheckInRecord.class));

        mockMvc.perform(post("/api/wx/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCheckin_EmptyReservationId() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("reservationId", "");
        params.put("seatId", "1");

        mockMvc.perform(post("/api/wx/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testCheckin_EmptySeatId() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("reservationId", "1");
        params.put("seatId", "");

        mockMvc.perform(post("/api/wx/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testCheckout_Success() throws Exception {
        CheckInRecord record = new CheckInRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setCheckInTime(LocalDateTime.now().minusHours(2));
        record.setStatus("active");

        when(checkInRecordMapper.findById(anyLong())).thenReturn(record);

        Map<String, String> params = new HashMap<>();
        params.put("checkInRecordId", "1");

        mockMvc.perform(post("/api/wx/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCheckout_RecordNotFound() throws Exception {
        when(checkInRecordMapper.findById(anyLong())).thenReturn(null);

        Map<String, String> params = new HashMap<>();
        params.put("checkInRecordId", "999");

        mockMvc.perform(post("/api/wx/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testCheckout_AlreadyCompleted() throws Exception {
        CheckInRecord record = new CheckInRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setCheckInTime(LocalDateTime.now().minusHours(2));
        record.setStatus("completed");

        when(checkInRecordMapper.findById(anyLong())).thenReturn(record);

        Map<String, String> params = new HashMap<>();
        params.put("checkInRecordId", "1");

        mockMvc.perform(post("/api/wx/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(params))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testGetCheckInRecords() throws Exception {
        CheckInRecord record = new CheckInRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("active");

        when(checkInRecordMapper.findByUserId(anyLong())).thenReturn(List.of(record));

        mockMvc.perform(get("/api/wx/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testGetCheckInRecords_WithDateRange() throws Exception {
        CheckInRecord record = new CheckInRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("active");

        when(checkInRecordMapper.findByUserIdAndDateRange(anyLong(), anyString(), anyString())).thenReturn(List.of(record));

        mockMvc.perform(get("/api/wx/checkin/records")
                .param("startDate", "2026-06-01")
                .param("endDate", "2026-06-16")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetCurrentCheckInStatus_Active() throws Exception {
        CheckInRecord record = new CheckInRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus("active");

        when(checkInRecordMapper.findActiveByUserId(anyLong())).thenReturn(record);

        mockMvc.perform(get("/api/wx/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isCheckedIn").value(true));
    }

    @Test
    void testGetCurrentCheckInStatus_NotCheckedIn() throws Exception {
        when(checkInRecordMapper.findActiveByUserId(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/wx/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isCheckedIn").value(false));
    }
}