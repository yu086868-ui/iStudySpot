package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class SeatServiceImplTest {

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private com.ycyu.istudyspotbackend.mapper.StudyRoomMapper studyRoomMapper;

    @InjectMocks
    private SeatServiceImpl seatService;

    private Seat testSeat;

    @BeforeEach
    void setUp() {
        testSeat = new Seat();
        testSeat.setId(1L);
        testSeat.setRoomId(1L);
        testSeat.setSeatNumber("A1");
        testSeat.setStatus("available");
        testSeat.setSeatType(1);
        testSeat.setRowNum(1);
        testSeat.setColNum(1);
    }

    @Test
    void testGetSeatList() {
        // 模拟自习室查询
        com.ycyu.istudyspotbackend.entity.StudyRoom studyRoom = new com.ycyu.istudyspotbackend.entity.StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("Study Room 1");
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        // 模拟座位列表查询
        List<Seat> seats = new ArrayList<>();
        seats.add(testSeat);
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        // 测试获取座位列表
        List<Seat> result = seatService.getSeatList(1L, "available", "1", 1, 1);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        // 验证方法调用
        verify(seatMapper, times(1)).findByRoomId(1L);
    }

    @Test
    void testGetSeatDetail() {
        // 模拟座位详情查询
        when(seatMapper.findById(1L)).thenReturn(testSeat);

        // 测试获取座位详情
        Seat seat = seatService.getSeatDetail(1L);

        // 验证结果
        assertNotNull(seat);
        assertEquals(1L, seat.getId());
        assertEquals("A1", seat.getSeatNumber());

        // 验证方法调用
        verify(seatMapper, times(1)).findById(1L);
    }

    @Test
    void testGetSeatDetailNotFound() {
        // 模拟座位不存在
        when(seatMapper.findById(1L)).thenReturn(null);

        // 测试获取座位详情
        Exception exception = assertThrows(RuntimeException.class, () -> {
            seatService.getSeatDetail(1L);
        });

        assertEquals("座位不存在", exception.getMessage());

        // 验证方法调用
        verify(seatMapper, times(1)).findById(1L);
    }
}
