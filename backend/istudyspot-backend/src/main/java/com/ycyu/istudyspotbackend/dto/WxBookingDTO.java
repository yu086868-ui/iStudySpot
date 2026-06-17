package com.ycyu.istudyspotbackend.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WxBookingDTO {
    private String studyRoomId;
    private String seatId;
    private String startTime;
    private String endTime;
    private String bookingType;

    public String getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(String studyRoomId) { this.studyRoomId = studyRoomId; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getBookingType() { return bookingType; }
    public void setBookingType(String bookingType) { this.bookingType = bookingType; }

    public LocalDateTime getParsedStartTime() {
        if (startTime == null) return null;
        try {
            return LocalDateTime.parse(startTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public LocalDateTime getParsedEndTime() {
        if (endTime == null) return null;
        try {
            return LocalDateTime.parse(endTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
