package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Seat;
import java.util.Map;

public interface SeatService {
    Map<String, Object> getSeatMap(Long roomId);
    Seat getSeatDetail(Long id);
    Map<String, Object> getSeatTimeline(Long seatId, String date);
    Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime);
}