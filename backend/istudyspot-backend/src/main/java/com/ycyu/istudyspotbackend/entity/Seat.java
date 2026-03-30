package com.ycyu.istudyspotbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Seat {
    private Long id;
    private Long roomId;
    private Long areaId;
    private String seatNumber;
    private Integer rowNum;
    private Integer colNum;
    private Integer seatType;  // 1-普通 2-VIP
    private String status;     // available, booked, occupied, unavailable
    private BigDecimal pricePerHour;
    private String description;
    private Integer hasPower;
    private Integer hasLamp;
    private Integer isWindow;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public Integer getRowNum() { return rowNum; }
    public void setRowNum(Integer rowNum) { this.rowNum = rowNum; }

    public Integer getColNum() { return colNum; }
    public void setColNum(Integer colNum) { this.colNum = colNum; }

    public Integer getSeatType() { return seatType; }
    public void setSeatType(Integer seatType) { this.seatType = seatType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getHasPower() { return hasPower; }
    public void setHasPower(Integer hasPower) { this.hasPower = hasPower; }

    public Integer getHasLamp() { return hasLamp; }
    public void setHasLamp(Integer hasLamp) { this.hasLamp = hasLamp; }

    public Integer getIsWindow() { return isWindow; }
    public void setIsWindow(Integer isWindow) { this.isWindow = isWindow; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}