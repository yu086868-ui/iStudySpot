package com.ycyu.istudyspotbackend.entity;

import java.time.LocalDateTime;

public class CheckInRecord {
    private Long id;
    private Long userId;
    private Long reservationId;
    private Long studyRoomId;
    private Long seatId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer duration;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(Long studyRoomId) { this.studyRoomId = studyRoomId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
