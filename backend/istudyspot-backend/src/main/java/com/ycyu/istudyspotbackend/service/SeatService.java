package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Seat;

import java.util.List;
import java.util.Map;

public interface SeatService {
    List<Seat> getSeatList(Long studyRoomId, String status, String type, Integer row, Integer col);
    Seat getSeatDetail(Long id);
    Map<String, Object> getSeatMap(Long roomId);
    Map<String, Object> getSeatTimeline(Long id, String date);
    Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime);
}