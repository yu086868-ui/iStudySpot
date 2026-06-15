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
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SeatLayoutMapper seatLayoutMapper;

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Seat> getSeatList(Long studyRoomId, String status, String type, Integer row, Integer col) {
        StudyRoom room = studyRoomMapper.findById(studyRoomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        List<Seat> seats = seatMapper.findByRoomId(studyRoomId);

        List<Order> activeOrders = orderMapper.findActiveByRoomId(studyRoomId);
        Map<Long, Order> seatOrderMap = new HashMap<>();
        for (Order order : activeOrders) {
            if (order.getSeatId() != null) {
                seatOrderMap.put(order.getSeatId(), order);
            }
        }

        for (Seat seat : seats) {
            String mappedStatus = mapSeatStatus(seat, seatOrderMap.get(seat.getId()));
            seat.setStatus(mappedStatus);
        }

        if (status != null && !status.isEmpty()) {
            seats = seats.stream()
                    .filter(s -> status.equals(s.getStatus()))
                    .collect(Collectors.toList());
        }
        if (type != null && !type.isEmpty()) {
            int seatType = "vip".equalsIgnoreCase(type) ? 2 : 1;
            int finalSeatType = seatType;
            seats = seats.stream()
                    .filter(s -> s.getSeatType() != null && s.getSeatType() == finalSeatType)
                    .collect(Collectors.toList());
        }
        if (row != null) {
            seats = seats.stream()
                    .filter(s -> row.equals(s.getRowNum()))
                    .collect(Collectors.toList());
        }
        if (col != null) {
            seats = seats.stream()
                    .filter(s -> col.equals(s.getColNum()))
                    .collect(Collectors.toList());
        }

        return seats;
    }

    private String mapSeatStatus(Seat seat, Order activeOrder) {
        int dbStatus;
        try {
            dbStatus = Integer.parseInt(seat.getStatus());
        } catch (NumberFormatException e) {
            return seat.getStatus();
        }

        switch (dbStatus) {
            case 0:
                return "unavailable";
            case 2:
                return "unavailable";
            case 1:
            default:
                break;
        }

        if (activeOrder != null) {
            String orderStatus = activeOrder.getStatus();
            if ("in_use".equals(orderStatus)) {
                return "in_use";
            } else if ("paid".equals(orderStatus) || "pending".equals(orderStatus)) {
                return "booked";
            }
        }

        return "available";
    }

    @Override
    public Seat getSeatDetail(Long id) {
        Seat seat = seatMapper.findById(id);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        List<Order> activeOrders = orderMapper.findActiveByRoomId(seat.getRoomId());
        for (Order order : activeOrders) {
            if (id.equals(order.getSeatId())) {
                String mappedStatus = mapSeatStatus(seat, order);
                seat.setStatus(mappedStatus);
                return seat;
            }
        }

        String mappedStatus = mapSeatStatus(seat, null);
        seat.setStatus(mappedStatus);
        return seat;
    }

    @Override
    public Map<String, Object> getSeatMap(Long roomId) {
        StudyRoom room = studyRoomMapper.findById(roomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        List<Seat> seats = seatMapper.findByRoomId(roomId);

        List<Order> activeOrders = orderMapper.findActiveByRoomId(roomId);
        Map<Long, Order> seatOrderMap = new HashMap<>();
        for (Order order : activeOrders) {
            if (order.getSeatId() != null) {
                seatOrderMap.put(order.getSeatId(), order);
            }
        }

        for (Seat seat : seats) {
            String mappedStatus = mapSeatStatus(seat, seatOrderMap.get(seat.getId()));
            seat.setStatus(mappedStatus);
        }

        int maxRow = 0;
        int maxCol = 0;
        for (Seat seat : seats) {
            if (seat.getRowNum() != null && seat.getRowNum() > maxRow) {
                maxRow = seat.getRowNum();
            }
            if (seat.getColNum() != null && seat.getColNum() > maxCol) {
                maxCol = seat.getColNum();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studyRoomId", roomId);
        result.put("rows", maxRow);
        result.put("cols", maxCol);
        result.put("seats", seats);
        return result;
    }

    @Override
    public SeatLayoutResponse getSeatLayout(Long roomId) {
        StudyRoom room = studyRoomMapper.findById(roomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        List<Seat> seats = seatMapper.findByRoomId(roomId);
        List<Order> activeOrders = orderMapper.findActiveByRoomId(roomId);
        Map<Long, Order> seatOrderMap = new HashMap<>();
        for (Order order : activeOrders) {
            if (order.getSeatId() != null) {
                seatOrderMap.put(order.getSeatId(), order);
            }
        }

        for (Seat seat : seats) {
            seat.setStatus(mapSeatStatus(seat, seatOrderMap.get(seat.getId())));
        }

        List<SeatLayoutItem> items = seatLayoutMapper.findByRoomId(roomId);
        int maxRow = 0;
        int maxCol = 0;

        for (Seat seat : seats) {
            if (seat.getRowNum() != null) {
                maxRow = Math.max(maxRow, seat.getRowNum());
            }
            if (seat.getColNum() != null) {
                maxCol = Math.max(maxCol, seat.getColNum());
            }
        }

        for (SeatLayoutItem item : items) {
            int itemRow = item.getRowNum() == null ? 0 : item.getRowNum();
            int itemCol = item.getColNum() == null ? 0 : item.getColNum();
            int itemHeight = item.getHeightUnits() == null ? 1 : item.getHeightUnits();
            int itemWidth = item.getWidthUnits() == null ? 1 : item.getWidthUnits();
            maxRow = Math.max(maxRow, itemRow + itemHeight - 1);
            maxCol = Math.max(maxCol, itemCol + itemWidth - 1);
        }

        SeatLayoutResponse response = new SeatLayoutResponse();
        response.setStudyRoomId(roomId);
        response.setStudyRoomName(room.getName());
        response.setRows(Math.max(maxRow, 1));
        response.setCols(Math.max(maxCol, 1));
        response.setCellSize(40);
        response.setLayoutMode(items.isEmpty() ? "grid" : "hybrid");
        response.setSeats(seats);
        response.setItems(items);
        response.setLegend(buildSeatLayoutLegend());
        return response;
    }

    private Map<String, Object> buildSeatLayoutLegend() {
        Map<String, Object> legend = new LinkedHashMap<>();
        legend.put("seat", List.of("available", "booked", "in_use", "unavailable"));
        legend.put("layoutItems", List.of("aisle", "door", "window", "pillar", "front_desk", "table", "booth", "lounge_counter", "plant", "wall", "zone_label"));
        return legend;
    }

    @Override
    public Map<String, Object> getSeatTimeline(Long seatId, String date) {
        Map<String, Object> result = new HashMap<>();
        return result;
    }

    @Override
    public Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", new BigDecimal("20.00"));
        result.put("hours", 2);
        return result;
    }
}
