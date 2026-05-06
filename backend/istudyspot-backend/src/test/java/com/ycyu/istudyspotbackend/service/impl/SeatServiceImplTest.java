package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceImplTest {

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private StudyRoomMapper studyRoomMapper;

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
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("Study Room 1");
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        List<Seat> seats = new ArrayList<>();
        seats.add(testSeat);
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        List<Seat> result = seatService.getSeatList(1L, "available", "1", 1, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(seatMapper, times(1)).findByRoomId(1L);
    }

    @Test
    void testGetSeatListWithNonExistentRoom() {
        when(studyRoomMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            seatService.getSeatList(999L, "available", "1", 1, 1);
        });

        assertEquals("自习室不存在", exception.getMessage());
    }

    @Test
    void testGetSeatListWithEmptySeats() {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        when(seatMapper.findByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSeatListWithNullParameters() {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        List<Seat> seats = new ArrayList<>();
        seats.add(testSeat);
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetSeatDetail() {
        when(seatMapper.findById(1L)).thenReturn(testSeat);

        Seat seat = seatService.getSeatDetail(1L);

        assertNotNull(seat);
        assertEquals(1L, seat.getId());
        assertEquals("A1", seat.getSeatNumber());

        verify(seatMapper, times(1)).findById(1L);
    }

    @Test
    void testGetSeatDetailNotFound() {
        when(seatMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            seatService.getSeatDetail(1L);
        });

        assertEquals("座位不存在", exception.getMessage());

        verify(seatMapper, times(1)).findById(1L);
    }

    @Test
    void testGetSeatMap() {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        List<Seat> seats = new ArrayList<>();
        seats.add(testSeat);
        
        Seat seat2 = new Seat();
        seat2.setId(2L);
        seat2.setRoomId(1L);
        seat2.setSeatNumber("B2");
        seat2.setRowNum(2);
        seat2.setColNum(2);
        seats.add(seat2);
        
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        assertEquals(1L, result.get("studyRoomId"));
        assertEquals(2, result.get("rows"));
        assertEquals(2, result.get("cols"));
        assertNotNull(result.get("seats"));
    }

    @Test
    void testGetSeatMapWithNonExistentRoom() {
        when(studyRoomMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            seatService.getSeatMap(999L);
        });

        assertEquals("自习室不存在", exception.getMessage());
    }

    @Test
    void testGetSeatMapWithEmptySeats() {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        when(seatMapper.findByRoomId(1L)).thenReturn(new ArrayList<>());

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        assertEquals(0, result.get("rows"));
        assertEquals(0, result.get("cols"));
        assertNotNull(result.get("seats"));
    }

    @Test
    void testGetSeatMapWithNullRowCol() {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setId(1L);
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        Seat seatWithNullCoords = new Seat();
        seatWithNullCoords.setId(1L);
        seatWithNullCoords.setRoomId(1L);
        seatWithNullCoords.setRowNum(null);
        seatWithNullCoords.setColNum(null);
        
        List<Seat> seats = new ArrayList<>();
        seats.add(seatWithNullCoords);
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        assertEquals(0, result.get("rows"));
        assertEquals(0, result.get("cols"));
    }

    @Test
    void testGetSeatTimeline() {
        Map<String, Object> result = seatService.getSeatTimeline(1L, "2024-01-01");

        assertNotNull(result);
    }

    @Test
    void testGetSeatTimelineWithNullDate() {
        Map<String, Object> result = seatService.getSeatTimeline(1L, null);

        assertNotNull(result);
    }

    @Test
    void testCalculatePrice() {
        Map<String, Object> result = seatService.calculatePrice(1L, "09:00", "11:00");

        assertNotNull(result);
        assertNotNull(result.get("totalAmount"));
        assertNotNull(result.get("hours"));
        assertEquals(new BigDecimal("20.00"), result.get("totalAmount"));
        assertEquals(2, result.get("hours"));
    }

    @Test
    void testCalculatePriceWithNullParameters() {
        Map<String, Object> result = seatService.calculatePrice(null, null, null);

        assertNotNull(result);
        assertNotNull(result.get("totalAmount"));
    }

    @Test
    void testGetSeatDetailWithNullId() {
        assertThrows(RuntimeException.class, () -> {
            seatService.getSeatDetail(null);
        });
    }

    @Test
    void testGetSeatListWithNullRoomId() {
        assertThrows(RuntimeException.class, () -> {
            seatService.getSeatList(null, "available", "1", 1, 1);
        });
    }

    @Test
    void testGetSeatMapWithNullRoomId() {
        assertThrows(RuntimeException.class, () -> {
            seatService.getSeatMap(null);
        });
    }
}
