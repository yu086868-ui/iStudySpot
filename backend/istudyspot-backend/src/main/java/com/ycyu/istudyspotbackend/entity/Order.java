package com.ycyu.istudyspotbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long seatId;
    private Long roomId;
    private String studyRoomName;
    private String roomName;
    private String seatPosition;
    private String seatNumber;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private BigDecimal totalPrice;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private Integer actualDuration;
    private BigDecimal actualPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createTime;

    @JsonProperty("startTime")
    public LocalDateTime getStartTime() { return planStartTime; }
    public void setStartTime(LocalDateTime startTime) { this.planStartTime = startTime; }

    @JsonProperty("endTime")
    public LocalDateTime getEndTime() { return planEndTime; }
    public void setEndTime(LocalDateTime endTime) { this.planEndTime = endTime; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getStudyRoomName() { return studyRoomName; }
    public void setStudyRoomName(String studyRoomName) { this.studyRoomName = studyRoomName; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getSeatPosition() { return seatPosition; }
    public void setSeatPosition(String seatPosition) { this.seatPosition = seatPosition; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }

    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }

    public Integer getActualDuration() { return actualDuration; }
    public void setActualDuration(Integer actualDuration) { this.actualDuration = actualDuration; }

    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }

    @JsonIgnore
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @JsonIgnore
    public LocalDateTime getPlanStartTime() { return planStartTime; }
    public void setPlanStartTime(LocalDateTime planStartTime) { this.planStartTime = planStartTime; }

    @JsonIgnore
    public LocalDateTime getPlanEndTime() { return planEndTime; }
    public void setPlanEndTime(LocalDateTime planEndTime) { this.planEndTime = planEndTime; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) { this.actualEndTime = actualEndTime; }

    @JsonProperty("createdAt")
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
