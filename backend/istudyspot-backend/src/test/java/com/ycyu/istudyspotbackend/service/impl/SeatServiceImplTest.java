package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.SeatLayoutItem;
import com.ycyu.istudyspotbackend.entity.SeatLayoutResponse;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.SeatLayoutMapper;
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
    private SeatLayoutMapper seatLayoutMapper;

    @Mock
    private StudyRoomMapper studyRoomMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private SeatServiceImpl seatService;

    private StudyRoom testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new StudyRoom();
        testRoom.setId(1L);
        testRoom.setName("Study Room 1");
    }

    private Seat createSeat(Long id, String status, Integer rowNum, Integer colNum, Integer seatType) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setRoomId(1L);
        seat.setSeatNumber("S" + id);
        seat.setStatus(status);
        seat.setSeatType(seatType);
        seat.setRowNum(rowNum);
        seat.setColNum(colNum);
        seat.setPricePerHour(BigDecimal.TEN);
        return seat;
    }

    private SeatLayoutItem createLayoutItem(Long roomId, String itemType, Integer rowNum, Integer colNum, Integer widthUnits, Integer heightUnits) {
        SeatLayoutItem item = new SeatLayoutItem();
        item.setRoomId(roomId);
        item.setItemType(itemType);
        item.setRowNum(rowNum);
        item.setColNum(colNum);
        item.setWidthUnits(widthUnits);
        item.setHeightUnits(heightUnits);
        item.setZIndex(1);
        return item;
    }

    @Test
    void testGetSeatList() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("available", result.get(0).getStatus());
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
    void testGetSeatListStatusUnavailable0() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "0", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("unavailable", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListStatusUnavailable2() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "2", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("unavailable", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListStatusBookedWithPaidOrder() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Order activeOrder = new Order();
        activeOrder.setSeatId(1L);
        activeOrder.setStatus("paid");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("booked", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListStatusInUseWithActiveOrder() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Order activeOrder = new Order();
        activeOrder.setSeatId(1L);
        activeOrder.setStatus("in_use");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("in_use", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListStatusBookedWithPendingOrder() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Order activeOrder = new Order();
        activeOrder.setSeatId(1L);
        activeOrder.setStatus("pending");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("booked", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListWithNonNumericStatus() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "available", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("available", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListFilterByStatus() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "0", 2, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, "available", null, null, null);

        assertEquals(1, result.size());
        assertEquals("available", result.get(0).getStatus());
    }

    @Test
    void testGetSeatListFilterByVipType() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "1", 2, 1, 2));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, "vip", null, null);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getSeatType());
    }

    @Test
    void testGetSeatListFilterByNormalType() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "1", 2, 1, 2));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, "normal", null, null);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getSeatType());
    }

    @Test
    void testGetSeatListFilterByRow() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "1", 2, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, 1, null);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRowNum());
    }

    @Test
    void testGetSeatListFilterByCol() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "1", 1, 2, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, 2);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getColNum());
    }

    @Test
    void testGetSeatListWithEmptySeats() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        when(seatMapper.findByRoomId(1L)).thenReturn(new ArrayList<>());
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSeatListWithOrderNullSeatId() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Order activeOrder = new Order();
        activeOrder.setSeatId(null);
        activeOrder.setStatus("in_use");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        List<Seat> result = seatService.getSeatList(1L, null, null, null, null);

        assertEquals("available", result.get(0).getStatus());
    }

    @Test
    void testGetSeatDetail() {
        Seat seat = createSeat(1L, "1", 1, 1, 1);
        when(seatMapper.findById(1L)).thenReturn(seat);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        Seat result = seatService.getSeatDetail(1L);

        assertNotNull(result);
        assertEquals("available", result.getStatus());
    }

    @Test
    void testGetSeatDetailNotFound() {
        when(seatMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            seatService.getSeatDetail(1L);
        });

        assertEquals("座位不存在", exception.getMessage());
    }

    @Test
    void testGetSeatDetailWithActiveOrder() {
        Seat seat = createSeat(1L, "1", 1, 1, 1);
        when(seatMapper.findById(1L)).thenReturn(seat);

        Order activeOrder = new Order();
        activeOrder.setSeatId(1L);
        activeOrder.setStatus("in_use");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        Seat result = seatService.getSeatDetail(1L);

        assertEquals("in_use", result.getStatus());
    }

    @Test
    void testGetSeatDetailWithNoMatchingActiveOrder() {
        Seat seat = createSeat(1L, "1", 1, 1, 1);
        when(seatMapper.findById(1L)).thenReturn(seat);

        Order activeOrder = new Order();
        activeOrder.setSeatId(999L);
        activeOrder.setStatus("in_use");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        Seat result = seatService.getSeatDetail(1L);

        assertEquals("available", result.getStatus());
    }

    @Test
    void testGetSeatMap() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        seats.add(createSeat(2L, "1", 2, 2, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        assertEquals(1L, result.get("studyRoomId"));
        assertEquals(2, result.get("rows"));
        assertEquals(2, result.get("cols"));
        assertNotNull(result.get("seats"));
    }

    @Test
    void testGetSeatLayoutWithHybridItems() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 3, 1, 1));
        seats.add(createSeat(2L, "1", 6, 7, 2));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<SeatLayoutItem> items = new ArrayList<>();
        items.add(createLayoutItem(1L, "aisle", 2, 4, 2, 6));
        items.add(createLayoutItem(1L, "front_desk", 1, 1, 2, 1));
        when(seatLayoutMapper.findByRoomId(1L)).thenReturn(items);

        SeatLayoutResponse result = seatService.getSeatLayout(1L);

        assertNotNull(result);
        assertEquals(1L, result.getStudyRoomId());
        assertEquals("Study Room 1", result.getStudyRoomName());
        assertEquals("hybrid", result.getLayoutMode());
        assertEquals(7, result.getCols());
        assertEquals(7, result.getRows());
        assertEquals(2, result.getItems().size());
        assertEquals(2, result.getSeats().size());
        assertNotNull(result.getLegend());
    }

    @Test
    void testGetSeatLayoutFallsBackToGrid() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 2, 3, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());
        when(seatLayoutMapper.findByRoomId(1L)).thenReturn(new ArrayList<>());

        SeatLayoutResponse result = seatService.getSeatLayout(1L);

        assertEquals("grid", result.getLayoutMode());
        assertEquals(2, result.getRows());
        assertEquals(3, result.getCols());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void testGetSeatLayoutWithNonExistentRoom() {
        when(studyRoomMapper.findById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> seatService.getSeatLayout(999L));

        assertEquals("自习室不存在", exception.getMessage());
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
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        when(seatMapper.findByRoomId(1L)).thenReturn(new ArrayList<>());
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        assertEquals(0, result.get("rows"));
        assertEquals(0, result.get("cols"));
    }

    @Test
    void testGetSeatMapWithNullRowCol() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        Seat seat = createSeat(1L, "1", null, null, 1);
        seats.add(seat);
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

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
        assertEquals(new BigDecimal("20.00"), result.get("totalAmount"));
        assertEquals(2, result.get("hours"));
    }

    @Test
    void testCalculatePriceWithNullParameters() {
        Map<String, Object> result = seatService.calculatePrice(null, null, null);
        assertNotNull(result);
    }

    @Test
    void testGetSeatMapWithActiveOrders() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);

        Order activeOrder = new Order();
        activeOrder.setSeatId(1L);
        activeOrder.setStatus("paid");
        List<Order> activeOrders = new ArrayList<>();
        activeOrders.add(activeOrder);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(activeOrders);

        Map<String, Object> result = seatService.getSeatMap(1L);

        assertNotNull(result);
        List<Seat> resultSeats = (List<Seat>) result.get("seats");
        assertEquals("booked", resultSeats.get(0).getStatus());
    }

    @Test
    void testGetSeatListWithEmptyStatusFilter() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, "", null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void testGetSeatListWithEmptyTypeFilter() {
        when(studyRoomMapper.findById(1L)).thenReturn(testRoom);
        List<Seat> seats = new ArrayList<>();
        seats.add(createSeat(1L, "1", 1, 1, 1));
        when(seatMapper.findByRoomId(1L)).thenReturn(seats);
        when(orderMapper.findActiveByRoomId(1L)).thenReturn(new ArrayList<>());

        List<Seat> result = seatService.getSeatList(1L, null, "", null, null);

        assertEquals(1, result.size());
    }
}
