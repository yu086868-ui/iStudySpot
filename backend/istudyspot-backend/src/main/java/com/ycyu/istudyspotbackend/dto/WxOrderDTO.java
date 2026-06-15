package com.ycyu.istudyspotbackend.dto;

import com.ycyu.istudyspotbackend.entity.Order;
import java.time.LocalDateTime;

public class WxOrderDTO {
    private String id;
    private String userId;
    private String studyRoomId;
    private String seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WxOrderDTO fromEntity(Order order) {
        WxOrderDTO dto = new WxOrderDTO();
        dto.setId(order.getId() != null ? order.getId().toString() : null);
        dto.setUserId(order.getUserId() != null ? order.getUserId().toString() : null);
        dto.setStudyRoomId(order.getRoomId() != null ? order.getRoomId().toString() : null);
        dto.setSeatId(order.getSeatId() != null ? order.getSeatId().toString() : null);
        dto.setStartTime(order.getPlanStartTime());
        dto.setEndTime(order.getPlanEndTime());
        dto.setStatus(order.getStatus());
        dto.setCheckInTime(order.getCheckinTime());
        dto.setCheckOutTime(order.getCheckoutTime());
        dto.setCreatedAt(order.getCreateTime());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStudyRoomId() { return studyRoomId; }
    public void setStudyRoomId(String studyRoomId) { this.studyRoomId = studyRoomId; }

    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
