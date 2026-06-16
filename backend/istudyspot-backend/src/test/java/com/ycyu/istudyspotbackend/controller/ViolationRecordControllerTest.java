package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.ViolationRecord;
import com.ycyu.istudyspotbackend.mapper.ViolationRecordMapper;
import com.ycyu.istudyspotbackend.mapper.UserMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ViolationRecordControllerTest {

    @Mock
    private ViolationRecordMapper violationRecordMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ViolationRecordController violationRecordController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(violationRecordController).build();
    }

    @Test
    void testGetViolationRecords() throws Exception {
        ViolationRecord record = new ViolationRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setType("迟到");
        record.setStatus("active");

        when(violationRecordMapper.findByUserId(anyLong())).thenReturn(Arrays.asList(record));
        when(violationRecordMapper.countByUserId(anyLong())).thenReturn(1);

        mockMvc.perform(get("/api/violations")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testGetViolationRecords_Empty() throws Exception {
        when(violationRecordMapper.findByUserId(anyLong())).thenReturn(Collections.emptyList());
        when(violationRecordMapper.countByUserId(anyLong())).thenReturn(0);

        mockMvc.perform(get("/api/violations")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void testSubmitAppeal_Success() throws Exception {
        ViolationRecord record = new ViolationRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setStatus("active");

        when(violationRecordMapper.findById(anyLong())).thenReturn(record);

        String body = objectMapper.writeValueAsString(Map.of("reason", "有正当理由"));

        mockMvc.perform(post("/api/violations/1/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testSubmitAppeal_RecordNotFound() throws Exception {
        when(violationRecordMapper.findById(anyLong())).thenReturn(null);

        String body = objectMapper.writeValueAsString(Map.of("reason", "理由"));

        mockMvc.perform(post("/api/violations/999/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testSubmitAppeal_NotOwner() throws Exception {
        ViolationRecord record = new ViolationRecord();
        record.setId(1L);
        record.setUserId(2L);
        record.setStatus("active");

        when(violationRecordMapper.findById(anyLong())).thenReturn(record);

        String body = objectMapper.writeValueAsString(Map.of("reason", "理由"));

        mockMvc.perform(post("/api/violations/1/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testSubmitAppeal_EmptyReason() throws Exception {
        ViolationRecord record = new ViolationRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setStatus("active");

        when(violationRecordMapper.findById(anyLong())).thenReturn(record);

        String body = objectMapper.writeValueAsString(Map.of("reason", "   "));

        mockMvc.perform(post("/api/violations/1/appeal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}